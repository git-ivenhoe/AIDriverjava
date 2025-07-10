package edu.cs4730.wearappvoice.utils;

import com.alibaba.dashscope.audio.asr.translation.TranslationRecognizerChat;
import com.alibaba.dashscope.audio.asr.translation.TranslationRecognizerParam;
import com.alibaba.dashscope.audio.asr.translation.results.TranscriptionResult;
import com.alibaba.dashscope.audio.asr.translation.results.TranslationRecognizerResult;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.common.Status;
import edu.cs4730.wearappvoice.ai.FloatingService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AliVoiceRecg {
    private static final String TAG = "AliVoiceRecg";
    private static final int PACKAGE_LEN = 1280*10;
    private static final int CANCEL_TIME_MS = 10000;
    private static final int FLUSH_DELAY_MS = 500;
    private TranslationRecognizerChat translator;
    private TranslationRecognizerParam param2;
    private ResultCallback<TranslationRecognizerResult> callback2 = null;
    
    private final byte[] buffer = new byte[2 * 1024 * 10];
    private int currentBufferLength = 0;

    private final BlockingQueue<byte[]> pwmQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean isRecging = new AtomicBoolean(false);
    private final AtomicBoolean isOpen = new AtomicBoolean(false);
    private Handler handler;
    private final Runnable hideRunnable = this::cancelListen;
    private final Runnable flushRunnable = this::flushTailData;
    private Thread uploadThread;
    private FloatingService mFloatingService;
    private Timer timeoutTimer = new Timer();  // 定时器实例
    private TimerTask flushTask;               // 当前的任务
    //test
    AudioDataSaver saver0 = new AudioDataSaver();

	public AliVoiceRecg(FloatingService _m) {
		// 创建识别器
        if (!isNetworkConnected()) {
            Constants.sendTTSMessage("网络异常");
            param2 = null;
            translator = null;
            callback2 = null;
            isOpen.set(false);
            return;
        }

        mFloatingService = _m;
        initHandlers();

        translator = new TranslationRecognizerChat();
        // 初始化请求参数
        param2 = TranslationRecognizerParam.builder()
                  // 若没有将API Key配置到环境变量中，需将your-api-key替换为自己的API Key
                  .apiKey(Constants.qw_apikey)
                  .model("gummy-chat-v1") // 设置模型名
                  .format("pcm") // 设置待识别音频格式，支持的音频格式：pcm、pcm编码的wav、mp3、ogg封装的opus、ogg封装的speex、aac、amr
                  .sampleRate(16000) // 设置待识别音频采样率（单位Hz）。仅支持16000Hz采样率。
                  .transcriptionEnabled(true) // 设置是否开启实时识别
                  .maxEndSilence(400)
                  .build();

		// 设置回调函数
        callback2 = new ResultCallback<TranslationRecognizerResult>() {
            @Override
            public void onEvent(TranslationRecognizerResult result) {
                // 打印最终结果
                if (result.getTranscriptionResult() != null) {
                    TranscriptionResult transcriptionResult = result.getTranscriptionResult();
                    if (result.isSentenceEnd()) {
                        Log.d(TAG, "最终结果: " + transcriptionResult.getText());
                        sendMessage(Constants.MESSAGE_SHOW_LLM_VOICE_TEXT, transcriptionResult.getText());
                        // 超时后关闭MIC监听
                        handler.removeCallbacks(hideRunnable);
                        // 20秒后隐藏文字框
                        handler.postDelayed(hideRunnable, CANCEL_TIME_MS);
                    }
                    else {
                        Log.d(TAG, "中间结果: " + transcriptionResult.getText());
                        sendMessage(Constants.MESSAGE_SHOW_VOICE_TEXT, transcriptionResult.getText());
                        // 超时后关闭MIC监听
                        handler.removeCallbacks(hideRunnable);
                    }
                }
            }

            @Override
            public void onComplete() {
                System.out.println("Translation complete");
            }

            @Override
            public void onError(Exception e) {

            }

            @Override
            public void onOpen(Status status) {
                isOpen.set(true);
            }
		};

        //test
        if (saver0.init("test_audioafter.pcm")) {

        }
	}

    private void initHandlers() {
        // 主处理 Handler（用于 UI/逻辑分发）
        new Thread(() -> {
            Looper.prepare();
            handler = new Handler();
            Looper.loop();
        }, "MainHandlerThread").start();

        // 等待 Handler 初始化完成（可根据项目中情况优化）
        while (handler == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void sendMessage(int what, Object obj) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = obj;
        Constants.mainHandle.sendMessage(msg);
    }

    public synchronized void recognizerStart() {
        if (isRecging.get()) return;

        isRecging.set(true);
        translator.call(param2, callback2);

        uploadThread = new Thread(() -> {
            try {
                while (isRecging.get()) {
                    byte[] frame = pwmQueue.poll(100, TimeUnit.MILLISECONDS);
                    if ((frame != null)) {
//                        Log.e(TAG, "sendAudioFrame 444444444");
                        translator.sendAudioFrame(ByteBuffer.wrap(frame));
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e(TAG, "识别线程中断: " + e.getMessage());
            }
        });
        uploadThread.start();
    }

    public synchronized void recognizerStop() {
        isRecging.set(false);
        if (uploadThread != null) {
            uploadThread.interrupt();
            uploadThread = null;
        }

        if (translator != null) {
            try {
                translator.stop();
            } catch (Exception ignored) {
            }
        }
    }

    private void flushTailData() {
        if (currentBufferLength > 0) {
            pwmQueue.offer(Arrays.copyOf(buffer, currentBufferLength));
            currentBufferLength = 0;
        }
        recognizerStop();
    }

	/**
	 * 接收 wav 数据，处理为 1280 分片并加入队列，
	 * 若最后一段不足 1280，则 200ms 内无数据则加入尾部数据。
	 */
    public void uploadWavDataToQw(byte[] wavData, int len) {
//        if(isRecging.get())
//            mFloatingService.changeGifIcon("/animate2.gif");

        // 取消前一次定时任务
        if (flushTask != null) {
            flushTask.cancel();
        }

        // 模拟写入音频数据
        //saver0.saveAudioData(wavData, len);

        int offset = 0;
        while (offset < len) {
            int remainingSpace = buffer.length - currentBufferLength;
            int dataToCopy = Math.min(len - offset, remainingSpace);

            System.arraycopy(wavData, offset, buffer, currentBufferLength, dataToCopy);
            currentBufferLength += dataToCopy;
            offset += dataToCopy;

            if (currentBufferLength >= PACKAGE_LEN) {
                pwmQueue.offer(Arrays.copyOf(buffer, PACKAGE_LEN));
                currentBufferLength = 0;
            }
        }

        // 创建并调度新的超时任务
        flushTask = new TimerTask() {
            @Override
            public void run() {
                flushRunnable.run();
            }
        };
        timeoutTimer.schedule(flushTask, FLUSH_DELAY_MS);

        recognizerStart();
    }

    public void cancelListen() {
        sendMessage(Constants.MESSAGE_STOP_LLM_RECG_LISTEN, null);
    }

	public static boolean isNetworkConnected() {
		try {
			// 尝试连接一个可靠网站
			URL url = new URL("http://www.sina.com");
			HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();
			urlConnect.setConnectTimeout(3000); // 3 秒超时
			urlConnect.connect();
			return true;//urlConnect.getResponseCode() == 200;
		} catch (IOException e) {
			return false;
		}
	}
}