package edu.cs4730.wearappvoice.voice;

import edu.cs4730.wearappvoice.ai.FloatingService;
import edu.cs4730.wearappvoice.rnnoise.Bytes;
import edu.cs4730.wearappvoice.rnnoise.Denoiser;
import edu.cs4730.wearappvoice.utils.*;
import org.vosk.RecognitionListener;
import org.vosk.Recognizer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Service that records audio in a thread, passes it to a recognizer and emits
 * recognition results. Recognition events are passed to a client using
 * {@link RecognitionListener}
 */
public class MySpeechService {

    private Recognizer recognizer;

    private int sampleRate;
    private static float BUFFER_SIZE_SECONDS = 0.2f;
    private int bufferSize;
    private AudioRecord recorder;
    private FloatingService mContext;

    private RecognizerThread recognizerThread;
    private ByteArrayOutputStream pcmBuffer; // 用于存储PCM数据

    private Handler mainHandler;
	private static final int SILENCE_THRESHOLD = 800; // 静音阈值，根据实际情况调整
    private FloatingService.WaveCallback mlistener;
    private AliVoiceRecg mAliVoiceRecg;
    private final Denoiser mDenoiser = new Denoiser();
    AudioDataSaver saver = new AudioDataSaver();

    SilenceDetector detector = new SilenceDetector(
            16000,    // 采样率
            20,       // 帧长 20ms
            20,       // 滑动窗大小（1秒）
            500,      // RMS静音门限
            0.8,      // 滑动窗中80%静音帧则为静音段
            3         // 中值滤波窗口大小（推荐 3）
    );
//    AdvancedSilenceDetector detector = new AdvancedSilenceDetector();
    /**
     * Creates speech service. Service holds the AudioRecord object, so you
     * need to call {@link #shutdown()} in order to properly finalize it.
     *
     * @throws IOException thrown if audio recorder can not be created for some reason.
     */
    public MySpeechService(Recognizer recognizer, FloatingService _m, float sampleRate, final FloatingService.WaveCallback listener) throws IOException {
        this.recognizer = recognizer;
        this.sampleRate = (int) sampleRate;
        this.mlistener = listener;

        this.mContext = _m;
        bufferSize = Math.round(this.sampleRate * BUFFER_SIZE_SECONDS);
        recorder = new AudioRecord();

        if (recorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
            recorder.release();
            throw new IOException("Failed to initialize recorder. Microphone might be already in use.");
        }

        mAliVoiceRecg = new AliVoiceRecg(mContext);
        new Thread(() -> {
            Looper.prepare();
            mainHandler = new Handler();
            Looper.loop();
        }).start();

        if (saver.init("test_audiobefore.pcm")) {

        }
    }


    /**
     * Starts recognition. Does nothing if recognition is active.
     *
     * @return true if recognition was actually started
     */
    public boolean startListening(RecognitionListener listener) {
        if (null != recognizerThread)
            return false;

        recognizerThread = new RecognizerThread(listener);
        recognizerThread.start();
        return true;
    }

    private boolean stopRecognizerThread() {
        if (null == recognizerThread)
            return false;

        try {
            recognizerThread.interrupt();
            recognizerThread.join();
        } catch (InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
        }

        recognizerThread = null;
        return true;
    }

    /**
     * Stops recognition. Listener should receive final result if there is
     * any. Does nothing if recognition is not active.
     *
     * @return true if recognition was actually stopped
     */
    public boolean stop() {
        return stopRecognizerThread();
    }

    /**
     * Cancel recognition. Do not post any new events, simply cancel processing.
     * Does nothing if recognition is not active.
     *
     * @return true if recognition was actually stopped
     */
    public boolean cancel() {
        if (recognizerThread != null) {
            recognizerThread.setPause(true);
        }
        return stopRecognizerThread();
    }

    /**
     * Shutdown the recognizer and release the recorder
     */
    public void shutdown() {
        recorder.release();
    }

    public void setPause(boolean paused) {
        if (recognizerThread != null) {
            recognizerThread.setPause(paused);
        }
    }

    /**
     * Resets recognizer in a thread, starts recognition over again
     */
    public void reset() {
        if (recognizerThread != null) {
            recognizerThread.reset();
        }
    }

    private final class RecognizerThread extends Thread {

        private int remainingSamples;
        private final int timeoutSamples;
        private final static int NO_TIMEOUT = -1;
        private volatile boolean paused = false;
        private volatile boolean reset = false;

        RecognitionListener listener;

        public RecognizerThread(RecognitionListener listener, int timeout) {
            this.listener = listener;
            if (timeout != NO_TIMEOUT)
                this.timeoutSamples = timeout * sampleRate / 1000;
            else
                this.timeoutSamples = NO_TIMEOUT;
            this.remainingSamples = this.timeoutSamples;
        }

        public RecognizerThread(RecognitionListener listener) {
            this(listener, NO_TIMEOUT);
        }

        /**
         * When we are paused, don't process audio by the recognizer and don't emit
         * any listener results
         *
         * @param paused the status of pause
         */
        public void setPause(boolean paused) {
            this.paused = paused;
        }

        /**
         * Set reset state to signal reset of the recognizer and start over
         */
        public void reset() {
            this.reset = true;
        }

        @Override
        public void run() {
            recorder.startRecording();
            if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
                recorder.stop();
                IOException ioe = new IOException("Failed to start recording. Microphone might be already in use.");
                mainHandler.post(() -> listener.onError(ioe));
            }

            short[] buffer = new short[bufferSize];
            ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();

            while (!interrupted()
                    && ((timeoutSamples == NO_TIMEOUT) || (remainingSamples > 0))) {
                int nread = recorder.read(buffer, 0, buffer.length);

                if (paused) {
                    continue;
                }

                if (reset) {
                    recognizer.reset();
                    reset = false;
                    continue;
                }

                if (nread < 0) {
                    System.out.println("==========");
                    continue;
                }

//                buffer = Bytes.toShorttArray(mDenoiser.process(Bytes.toByteArray(buffer)));
                // 静音检测：计算buffer中的最大振幅
                boolean isSilent = true;
//                for (int i = 0; i < nread; i++) {
//                    if (Math.abs(buffer[i]) > SILENCE_THRESHOLD) {
//                        isSilent = false;
//                        break;
//                    }
//                }

                amplifyPCM(buffer, nread, 1.5f);
                // 模拟写入音频数据
//                byte[] tmp = Bytes.toByteArray(buffer);
 //               saver.saveAudioData(tmp, tmp.length);

                isSilent =  detector.processFrame(shortArrayToByteArray(buffer, nread));
                if (isSilent) {
                    System.out.println("当前是静音段！");
                    continue;
                } else {
                    isSilent = false;
                }

                if (!isSilent) {
                    if (Constants.isRecoByCloud == 2) {
                        if (Constants.AI_Status != Constants.AI_STATUS_SLEEP) {
                            // 如果不是静音段，将 short[] buffer 转换为 byte[] 数据并写入 ByteArrayOutputStream
                            byte[] byteBuffer = shortArrayToByteArray(buffer, nread);
                            bufferStream.write(byteBuffer, 0, byteBuffer.length);
                            if (mAliVoiceRecg != null) {
                                mAliVoiceRecg.uploadWavDataToQw(byteBuffer, byteBuffer.length);
//                                mlistener.onWave(byteBuffer); //画波形
                            }

                            continue;
                        }
                    }

                    if (recognizer.acceptWaveForm(buffer, nread)) {
                        byte[] wavData = new byte[0];
                        byte[] pcmData;

                        String result = recognizer.getResult();
                        if (FloatingService.getLastBetweenQuotes(result).length() >= 2) {
                            Log.i("小模型识别结果 ======= ", result);
                            switch (Constants.isRecoByCloud) {
                                case 0:
                                    mainHandler.post(() -> listener.onResult(result));
                                    break;
                                case 1:
                                    //系统语音识别，不在此处返回识别信息
                                    break;
                                case 2:
                                    //在语音唤醒休眠状态，发送唤醒词
                                    if (Constants.AI_Status == Constants.AI_STATUS_SLEEP) {
                                        mainHandler.post(() -> listener.onResult(result));
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                        // 重置缓存的 ByteArrayOutputStream
                        bufferStream.reset();
                    } else {
                        final String partialResult = recognizer.getPartialResult();
                        mainHandler.post(() -> listener.onPartialResult(partialResult));
                    }

                    if (timeoutSamples != NO_TIMEOUT) {
                        remainingSamples = remainingSamples - nread;
                    }
                }

//                recorder.stop();
                if (!paused) {
                    // If we met timeout signal that speech ended
                    if (timeoutSamples != NO_TIMEOUT && remainingSamples <= 0) {
                        mainHandler.post(() -> listener.onTimeout());
                    } else {
                        final String finalResult = recognizer.getFinalResult();
                        mainHandler.post(() -> listener.onFinalResult(finalResult));
                    }
                }
            }
        }
    }

    public void SetRecogType(int _type)
    {
        if(_type == 1)
        {
            stop();
            cancel();
        }
        else
        {
            startListening((RecognitionListener) mContext);
        }
    }

    /**
     * 将 short[] 转换为 byte[] 的实用方法
     */
    private byte[] shortArrayToByteArray(short[] shortArray, int length) {
        byte[] byteArray = new byte[length * 2];
        for (int i = 0; i < length; i++) {
            byteArray[i * 2] = (byte) (shortArray[i] & 0x00FF);
            byteArray[i * 2 + 1] = (byte) ((shortArray[i] >> 8) & 0x00FF);
        }
        return byteArray;
    }

    private byte[] shortArrayToByteArray2(short[] shortArray, int length) {
        byte[] byteArray = new byte[length * 2];
        for (int i = 0; i < length; i++) {
            byteArray[i * 2 + 1] = (byte) (shortArray[i] & 0x00FF);
            byteArray[i * 2] = (byte) ((shortArray[i] >> 8) & 0x00FF);
        }
        return byteArray;
    }

    /**
     * 对 short[] PCM 数据进行增益放大（就地修改）
     * @param pcmData PCM 数据数组（每个元素为一个采样点）
     * @param length  需要放大的采样点数量（通常为 pcmData.length，但也可小于它）
     * @param gain    增益倍数（如 1.5f 表示放大 1.5 倍）
     */
    public static void amplifyPCM(short[] pcmData, int length, float gain) {
        if (pcmData == null || length <= 0 || length > pcmData.length) return;

        for (int i = 0; i < length; i++) {
            int amplified = Math.round(pcmData[i] * gain);

            // 防止溢出
            if (amplified > Short.MAX_VALUE) amplified = Short.MAX_VALUE;
            if (amplified < Short.MIN_VALUE) amplified = Short.MIN_VALUE;

            pcmData[i] = (short) amplified;
        }
    }

}
