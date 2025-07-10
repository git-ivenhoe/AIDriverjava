package edu.cs4730.wearappvoice.ai;

import edu.cs4730.wearappvoice.net.SendAudioTask;
import edu.cs4730.wearappvoice.utils.*;
import edu.cs4730.wearappvoice.voice.MySpeechService;
import edu.cs4730.wearappvoice.voice.commandProcess;
import edu.cs4730.wearappvoice.voice.request.LoginScheduler;
import edu.cs4730.wearappvoice.voice.response.CommandData;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.vosk.*;

import javax.net.ssl.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Timer;
import java.util.TimerTask;

public class FloatingService implements RecognitionListener {
    private JFrame frame;
    private TextBubblePanel textLabel;
    private JPanel waveformPanel; // 模拟 mColorfulWaveformView
    private Timer timer;
    private final long hideTime = 20_000L; // 20 秒

    private Handler handler;
    private Runnable hideRunnable;
    private commandProcess mcommandProcess;
    private long ttsStartTime = 0; // 用于记录MESSAGE_START_TTS的时间
    private ColorfulWaveformView mColorfulWaveformView;
    private JLabel gifLabel;

    public static void main(String[] args) {
        new FloatingService();
    }

    public FloatingService() {
        System.out.println(System.getProperty("file.encoding"));
        try {
            //Java 程序启动时指定支持 TLS 1.2，支持https
            disableSslVerification();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.init();
        frame = new JFrame("Floating View");
        frame.setUndecorated(true); // 无边框窗口，类似悬浮窗
        frame.setBackground(new Color(0, 0, 0, 0)); // 完全透明
        frame.setAlwaysOnTop(true);
		frame.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0)); // 左对齐横向排列

        // 模拟 waveformPanel
        waveformPanel = new JPanel();
        waveformPanel.setBackground(Color.CYAN);
        frame.add(waveformPanel, BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);

        timer = new Timer();

        Constants.tts = new TTS();
        Constants.notifyAudioPlayer = new NotifyAudioPlayer();
        // 创建包含 GIF 的 JLabel 并缩放为 128x128，同时保持动画
        ImageIcon originalIcon = new ImageIcon(getClass().getResource("/robotIcon2.png"));

        // 创建一个 JLabel 显示动画，但设置大小限制
        gifLabel = new JLabel(originalIcon) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(128, 128);
            }

            @Override
            protected void paintComponent(Graphics g) {
                // 缩放并绘制 GIF 动画帧
                Image image = ((ImageIcon) getIcon()).getImage();
                g.drawImage(image, 0, 0, 128, 128, this);
            }
        };
        gifLabel.setPreferredSize(new Dimension(128, 128));
        gifLabel.setOpaque(false);

        // 添加点击事件监听器
        gifLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("GIF 被点击了！");
                URL resourceUrl = getClass().getResource("/notify.mp3");
                Constants.notifyAudioPlayer.playAudio(resourceUrl, new NotifyAudioPlayer.OnAudioPlayCompleteListener() {
                    @Override
                    public void onAudioPlayComplete() {
                        hideText();
                        Constants.tts.stop();
                        Constants.AI_Status = Constants.AI_STATUS_IDEL;
                        if(Constants.speechService != null) {
                            Constants.speechService.SetRecogType(Constants.isRecoByCloud);
                        }
                    }
                });
            }
        });

        //设置长按事件
        setupLongPress(gifLabel, frame);

		// 2. 创建文字面板（带背景图、可拉伸）
        textLabel = new TextBubblePanel("我是AI语音助手");
        frame.add(gifLabel);
        frame.add(textLabel);
        LibVosk.setLogLevel(LogLevel.INFO);
        //启动登录AI后台的操作，每个12小时执行一次登录，确保后台缓存正常
        LoginScheduler.startLoginTask();

        // 初始化 Handler 和 Runnable
        new Thread(() -> {
            Looper.prepare(); // 必须执行
            handler = new Handler();
            Looper.loop();    // 启动消息循环
        }).start();

        new Thread(() -> {
            Looper.prepare();
            Constants.mainHandle = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case Constants.MESSAGE_START_TTS:
                            Log.i("FloatingService", "Receive Message --- MESSAGE_START_TTS");
                            pause(true);
                            ttsStartTime = System.currentTimeMillis(); // 记录开始时间
                            if(msg.obj != null)
                                Constants.tts.startTTS(msg.obj.toString());
                            break;
                        case Constants.MESSAGE_END_TTS:
                            Log.i("FloatingService", "Receive Message --- MESSAGE_END_TTS");
                            long ttsEndTime = System.currentTimeMillis(); // 获取结束时间
                            long ttsDuration = ttsEndTime - ttsStartTime; // 计算时间差
                            Log.i("FloatingService", "TTS Duration: " + ttsDuration + " ms"); // 打印时间差
                            if(Constants.isRecoByCloud == 1)
                            {
                                if(Constants.speechService != null) {
                                    Constants.speechService.SetRecogType(Constants.isRecoByCloud);
                                }
                            }
                            else {
                                new Thread(() -> {
                                    Looper.prepare();
                                    new Handler().postDelayed(new Runnable(){
                                        @Override
                                        public void run(){
                                            pause(false);
                                        }
                                    }, 500);
                                    Looper.loop();
                                }).start();

                            }
                            break;
                        case Constants.MESSAGE_AI_VOICE_RECG:
                            Log.i("FloatingService", "Receive Message --- MESSAGE_AI_VOICE_RECG");
                            pause(true);
                            if(msg.obj != null) {
                                Log.i("uploadWavData --- VOICE命令返回:" , msg.obj.toString());
                                ResponseSuccess(msg.obj.toString());
                            }
                            break;
                        case Constants.MESSAGE_GOOGLE_VOICE_RECG:
                            Log.i("FloatingService", "Receive Message --- MESSAGE_GOOGLE_VOICE_RECG");
//                        pause(true);
                            if(msg.obj != null) {
                                onResult(msg.obj.toString());
                                //ResponseSuccess(msg.obj.toString());
                            }
                            break;
                        case Constants.MESSAGE_WAIT_AI_VOICE_RECG:
                            Log.i("FloatingService", "Receive Message --- MESSAGE_WAIT_AI_VOICE_RECG");
                            new edu.cs4730.wearappvoice.voice.Message(getLastBetweenQuotes(msg.obj.toString()), true, false, false, Constants.voicefile).AddAndUpdateList();
                            showTextAndPlay(getLastBetweenQuotes(msg.obj.toString()));
                            break;

                        case Constants.MESSAGE_SET_PLAY_VOICE:
                            Log.i("FloatingService", "Receive Message --- MESSAGE_SET_PLAY_VOICE");
                            playVoiceMessage(msg.obj.toString());
                            break;

                        case Constants.MESSAGE_SHOW_VOICE_TEXT:
                            Log.i("FloatingService", "Receive Message --- MESSAGE_SHOW_VOICE_TEXT");
                            if((msg.obj.toString() !=null) && (msg.obj.toString().length() > 0))
                                showText(msg.obj.toString());
                            break;
                        case Constants.MESSAGE_SHOW_LLM_VOICE_TEXT:
                            Log.i("FloatingService", "Receive Message --- MESSAGE_SHOW_LLM_VOICE_TEXT");
                            if((msg.obj.toString() !=null) && (msg.obj.toString().length() > 0)) {
                                showText(msg.obj.toString());
                                onResult(msg.obj.toString());
                            }
                            break;
                        case Constants.MESSAGE_VOICE_RECG_ERROR:
                            Log.i("FloatingService", "Receive Message --- MESSAGE_VOICE_RECG_ERROR");
//                            URL resourceUrl = getClass().getResource("/notify.mp3");
//                            Constants.notifyAudioPlayer.playAudio(resourceUrl,  new OnAudioPlayCompleteListener() {
//                            @Override
//                            public void onAudioPlayComplete() {
//                                onLocalSpeechRecognition();
//                                Constants.AI_Status = Constants.AI_STATUS_SLEEP;
//                                if(Constants.speechService != null) {
//                                    Constants.speechService.SetRecogType(Constants.isRecoByCloud);
//                                }
//                            }
//                        });
                            break;
                        case Constants.MESSAGE_STOP_LLM_RECG_LISTEN://关闭MIC监听
                            Log.i("FloatingService", "Receive Message --- MESSAGE_STOP_LLM_RECG_LISTEN");
                            changeGifIcon("/robotIcon2.png");
                            URL resourceUrl = getClass().getResource("/notify.mp3");
                            Constants.notifyAudioPlayer.playAudio(resourceUrl,  new NotifyAudioPlayer.OnAudioPlayCompleteListener() {
                                @Override
                                public void onAudioPlayComplete() {
                                    Constants.AI_Status = Constants.AI_STATUS_SLEEP;
                                    if(Constants.speechService != null) {
//                                    Constants.speechService.cancel();
                                    }
                                }
                            });
                            break;
                        default:
                            break;
                    }
                };
            };
            Looper.loop();
        }).start();

        hideRunnable = this::hideText;
        mcommandProcess = new commandProcess(null, null, null, null);

        initModel();
        Constants.notifyAudioPlayer = new NotifyAudioPlayer();

        // 设置布局和添加组件
        frame.pack();
        // 设置窗口位置（屏幕中间或指定位置）
        frame.setVisible(true);
    }

    public void changeGifIcon(String resname) {
        SwingUtilities.invokeLater(() -> {
            URL gifUrl = getClass().getResource(resname);
            if (gifUrl == null) {
                System.err.println("资源未找到: " + resname);
                return;
            }
            ImageIcon newIcon = new ImageIcon(gifUrl);
            gifLabel.setIcon(newIcon);
            gifLabel.revalidate();
            gifLabel.repaint();  // 可选，加快刷新
        });
    }

    // 显示文本并隐藏 waveformPanel
    public void showText(String text) {
        textLabel.setTextContent(text);
        textLabel.setVisible(true);
        waveformPanel.setVisible(false);

        // 移除之前的延迟任务
        timer.cancel();
        timer = new Timer();

        // 20 秒后隐藏文本
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> hideText());
            }
        }, hideTime);
    }

    // 隐藏文本并清除内容
    public void hideText() {
        textLabel.setTextContent("");
        textLabel.setVisible(false);
        waveformPanel.setVisible(true);
    }

    public void showPopupMenu() {
            JFrame frame = new JFrame("Swing PopupMenu 示例");
            JButton button = new JButton("右键点击我");

            // 创建弹出菜单
            JPopupMenu popupMenu = new JPopupMenu();

            JMenuItem debugItem = new JMenuItem("调试界面");
            JMenuItem configItem = new JMenuItem("配置界面");
            // JMenuItem recgItem = new JMenuItem("识别界面"); // 可选

            popupMenu.add(debugItem);
            popupMenu.add(configItem);
            // popupMenu.add(recgItem);

            // 添加菜单项点击事件
            debugItem.addActionListener(e -> showDebugWindow());
            configItem.addActionListener(e -> showConfigWindow());
            // recgItem.addActionListener(e -> showRecgWindow());

            frame.add(button);
            frame.setSize(300, 200);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }

        // 模拟打开调试窗口
        private static void showDebugWindow() {
            JOptionPane.showMessageDialog(null, "打开调试界面");
            // 或者 new DebugWindow().setVisible(true);
        }

        // 模拟打开配置窗口
        private static void showConfigWindow() {
            JOptionPane.showMessageDialog(null, "打开配置界面");
            // 或者 new ConfigWindow().setVisible(true);
        }

        // 可选：识别窗口
        private static void showRecgWindow() {
            JOptionPane.showMessageDialog(null, "打开识别界面");
        }

    private void onGoogleSpeechRecognition() {
        // TODO: 添加谷歌语音识别的实现
        Constants.isRecoByCloud = 1;  // Google云语音识别
    }

    private void pause(boolean checked) {
        if (Constants.speechService != null) {
            Constants.speechService.setPause(checked);
        }
    }

    //同步阻塞播放
    public void showTextAndPlayAnsy(String text) {
        showText(text);

        // 移除之前的隐藏任务
        handler.removeCallbacks(hideRunnable);
        // 20秒后隐藏文字框
        handler.postDelayed(hideRunnable, hideTime);
        Constants.tts.startAnsyTTS(text);
    }

    // 显示文本
    public void showTextAndPlay(String text) {
        showText(text);

        // 语音播放显示的内容
        Constants.sendTTSMessage(text);

        // 移除之前的隐藏任务
        handler.removeCallbacks(hideRunnable);

        // 20秒后隐藏文字框
        handler.postDelayed(hideRunnable, hideTime);
    }

    private void initModel() {
        try {
            Constants.model = new Model("/usr/src/vosk-model-small-cn-0.22");
            Constants.mSendAudioTask = new SendAudioTask(null, Constants.VOICE_SERVER, Constants.VOICE_PORT);
            recognizeMicrophone();
            showTextAndPlay("你好，我是你的助理小卡，有问题随时找我");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void recognizeMicrophone() {
        if (Constants.speechService != null) {
            Constants.speechService.stop();
            Constants.speechService = null;
        } else {
            try {
                Recognizer rec = new Recognizer(Constants.model, 16000.0f);
                Constants.speechService = new MySpeechService(rec, this, 16000.0f, new WaveCallback() {
                    @Override
                    public void onWave(byte[] buffer) {
                        WaveShow(buffer);
                    }
                });
                Constants.speechService.startListening(this);
//                Constants.speechService.setPause(true);
            } catch (IOException e) {
            }
        }
    }

    @Override
    public void onPartialResult(String hypothesis) {

    }

    @Override
    public void onResult(String hypothesis) {
        //唤醒操作
        Log.i("getCommandRtn --- 语音识别返回：", hypothesis);
        if(Constants.ttsplaying) //正在播放语言时识别的东西都丢弃
        {
            return;
        }

        switch(Constants.AI_Status)
        {
            case Constants.AI_STATUS_SLEEP:
                String sourcePinyin = PinyinComparator.convertToPinyin(hypothesis);
                if (sourcePinyin.contains("xiao") && sourcePinyin.contains("ka"))
                {
                    showTextAndPlay("你好，我在");
                    Constants.AI_Status = Constants.AI_STATUS_IDEL;
                }
                return;
            case Constants.AI_STATUS_IDEL:
                if((hypothesis.indexOf("退出") >= 0) || (hypothesis.indexOf("退下") >= 0))
                {
                    Constants.AI_Status = Constants.AI_STATUS_SLEEP;
                    showTextAndPlay("再见，需要的时候再叫我吧");
                }
                break;
            default:
                return;
        }
        hypothesis = getLastBetweenQuotes(hypothesis);
        if ((hypothesis == null) || hypothesis.length() < 2) {
            return;
        }

        new edu.cs4730.wearappvoice.voice.Message(hypothesis, true, false, false, Constants.voicefile).AddAndUpdateList();
        Constants.mSendAudioTask.getCommandRtn(hypothesis, new SendAudioTask.UploadCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i("getCommandRtn --- TEXT命令返回", response);
                URL resourceUrl = getClass().getResource("/notify.mp3");
                Constants.notifyAudioPlayer.playAudio(resourceUrl, new NotifyAudioPlayer.OnAudioPlayCompleteListener() {
                    @Override
                    public void onAudioPlayComplete() {
                        ResponseSuccess(response);
                    }
                });
                hideText();
            }

            @Override
            public void onFailure(String errorMessage) {
                String errorMsg = errorMessage;
                new edu.cs4730.wearappvoice.voice.Message(errorMsg, false, false, false, null).AddAndUpdateList();;
                URL resourceUrl = getClass().getResource("/error.mp3");
                Constants.notifyAudioPlayer.playAudio(resourceUrl, new NotifyAudioPlayer.OnAudioPlayCompleteListener() {
                    @Override
                    public void onAudioPlayComplete() {
                        showTextAndPlay(errorMsg);
                    }
                });
            }
        });
    }

    @Override
    public void onFinalResult(String hypothesis) {

    }

    @Override
    public void onError(Exception exception) {

    }

    @Override
    public void onTimeout() {

    }

    private void ResponseSuccess(String response) {
        CommandData cmd = mcommandProcess.handleJsonResponse(response);
        if (cmd == null) {
            new edu.cs4730.wearappvoice.voice.Message(response, false, false, false, null).AddAndUpdateList();;
            Constants.sendTTSMessage(response);
            return;
        }

        if (cmd.cmd == null) {
            new edu.cs4730.wearappvoice.voice.Message(cmd.textResponse, false, false, false, null).AddAndUpdateList();;
            if(cmd.textResponse != null)
            {
                showTextAndPlay(cmd.textResponse);
            }
            return;
        }

        String temp = getLastBetweenQuotes(response);
        boolean isnav = false;
        switch (cmd.cmd) {
            case "call":
                temp = "好的,给 " + cmd.contactName + "打电话";
                return; //不需要TTS播报
            case "call-hangup":
                temp = "好的,挂断电话";
                break;
            case "call-answer":
                temp = "好的,接听电话";
                break;
            case "nav":
            case "nav_query":
                temp = "好的,导航到:" + cmd.destination;
                isnav = true;
                break;
            case "play_music":
                if(cmd.musicName == null)
                    return;
                if(cmd.musicName.equalsIgnoreCase("1"))
                {
                    temp = "好的,播放下一首音乐";
                }
                else if(cmd.musicName.equalsIgnoreCase("-1"))
                {
                    temp = "好的,播放上一首音乐";
                }
                else
                {
                    temp = "好的,播放:" + cmd.musicName;
                }
                break;
            case "set_volume":
            case "adjust_volume":

                return;
            case "adjust_temperature":
                if((cmd.level == null) || (cmd.level == 0))
                {
                    temp = "好的,调整空调温度";
                }
                else {
                    if (cmd.level > 0) {
                        temp = "好的,把温度调高" + cmd.level + "度";
                    } else {
                        temp = "好的,把温度调低" + cmd.level + "度";
                    }
                }
                break;
            case "adjust_windows":
                if(cmd.destination == null)
                    return;
                if(cmd.destination.equalsIgnoreCase("open"))
                {
                    temp = "好的,打开车窗";
                }
                else if(cmd.destination.equalsIgnoreCase("close"))
                {
                    temp = "好的,关闭车窗";
                }
                else
                {
                    temp = "好的,请问你要打开车窗吗?";
                }
                break;
            case "set_alarm":
                temp = "好的,已经设置了" + cmd.destination + "提醒";
                break;
            case "search_nearby": //查询周边
                temp = cmd.textResponse;
                break;
            default:
                Log.e("JSON Response", "Unknown command: " + cmd);
                if(cmd.textResponse == null)
                    return;
                if(cmd.textResponse.length() > 100) {
                    cmd.textResponse = cmd.textResponse.substring(0, 99) + "...";
                }
                if(cmd.textResponse.indexOf("cmd") >= 0)
                {
                    showTextAndPlay(cmd.input);
                }
                else
                {
                    showTextAndPlay(cmd.textResponse);
                }

                new edu.cs4730.wearappvoice.voice.Message(cmd.textResponse, false, false, false, null).AddAndUpdateList();;
                return;
        }

        new edu.cs4730.wearappvoice.voice.Message(temp, false, false, isnav, null).AddAndUpdateList();;
        showTextAndPlay(temp);
    }

    public static String getLastBetweenQuotes(String input) {
        if (input == null || input.length() < 4) {
            return ""; // 输入无效或长度不足
        }

        // 查找最后两个双引号的位置
        int lastQuoteIndex = input.lastIndexOf("\"");
        if (lastQuoteIndex == -1) {
            return input; // 没有找到双引号
        }

        int secondLastQuoteIndex = input.lastIndexOf("\"", lastQuoteIndex - 1);
        if (secondLastQuoteIndex == -1) {
            return ""; // 只有一个双引号
        }

        // 提取两个双引号之间的字符串
        if (secondLastQuoteIndex + 1 < lastQuoteIndex) {
            return input.substring(secondLastQuoteIndex + 1, lastQuoteIndex);
        } else {
            return ""; // 两个双引号是相邻的，没有内容在中间
        }
    }

    public void playVoiceMessage(String voiceFilePath) {
        if (voiceFilePath == null || voiceFilePath.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No voice file available");
            return;
        }

        // 初始化 JavaFX 环境（必须）
        new JFXPanel();
        try {
            File file = new File(voiceFilePath);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(null, "Voice file not found: " + voiceFilePath);
                return;
            }

            Media media = new Media(file.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);

            pause(true); // 模拟暂停其它播放逻辑

            mediaPlayer.setOnReady(() -> {
                mediaPlayer.play();
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.dispose(); // 释放资源
                pause(false);
            });

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to play voice file");
            pause(false);
        }
    }


    public interface WaveCallback {
        void onWave(byte[] buffer);
    }

    private void WaveShow0(byte[] buffer)
    {
        if(!mColorfulWaveformView.isVisible())
        {
            mColorfulWaveformView.setVisible(true);
        }

        short[] shorts = bytesToShorts(buffer);
        handler.post(new Runnable() {
            @Override
            public void run() {
                // 在这里更新 floatingView，例如更改文本或背景
                mColorfulWaveformView.setAmplitudes(normalizeAudioData(shorts));
            }
        });
    }

    private Runnable hideRunnable1;
    private long lastUpdateTime = 0;
    private static final long TIMEOUT_DELAY = 500; // 超时1秒

    private void WaveShow(byte[] buffer)
    {
        // 更新最后一次调用时间
        lastUpdateTime = System.currentTimeMillis();

        // 每次调用时，重置延时任务
        if (hideRunnable1 != null) {
            handler.removeCallbacks(hideRunnable1);
        }

        short[] shorts = bytesToShorts(buffer);
        // 在这里更新 floatingView，例如更改文本或背景
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!mColorfulWaveformView.isVisible())
                {
                    mColorfulWaveformView.setVisible(true);
                }
                mColorfulWaveformView.setAmplitudes(normalizeAudioData(shorts));
            }
        });

        // 设置新的延时任务，1秒后隐藏
        hideRunnable1 = new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                // 如果当前时间距离最后更新时间超过1秒，隐藏 mColorfulWaveformView
                if (currentTime - lastUpdateTime >= TIMEOUT_DELAY) {
                    mColorfulWaveformView.setVisible(false);
                }
            }
        };

        // 设置1秒后执行隐藏
        handler.postDelayed(hideRunnable1, TIMEOUT_DELAY);
    }


    private short[] bytesToShorts(byte[] bytes) {
        short[] shorts = new short[bytes.length / 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }

    // normalizeAudioData 方法用于将音频数据归一化处理
    private float[] normalizeAudioData(short[] audioData) {
        float[] normalizedData = new float[audioData.length];
        for (int i = 0; i < audioData.length; i++) {
            normalizedData[i] = audioData[i] / (float) Short.MAX_VALUE;
        }
        return normalizedData;
    }


    //三方应用调用大模型
   /* private BroadcastReceiver commandToLLMReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String command = intent.getStringExtra("command");
            // 更新UI或处理新数据
            Constants.mSendAudioTask.getCommandRtn(command, new SendAudioTask.UploadCallback() {
                @Override
                public void onSuccess(String response) {
                    Log.i("commandToLLMReceiver --- onSuccess LLM命令返回", response);
                    //发送广播回三方APP
                    Intent broadcastIntent = new Intent("COMMAND_TO_LLM_RESP");
                    broadcastIntent.putExtra("response", response);
                    sendBroadcast(broadcastIntent);

                    //增加到测试页面中
                    new edu.cs4730.wearappvoice.voice.Message(response, false, false, false, null).AddAndUpdateList();;
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e("commandToLLMReceiver --- onFailure LLM命令返回", errorMessage);
                }
            });
        }
    };*/


    public void disableSslVerification() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {}

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {}
                }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    private static Timer longPressTimer;
    private static final int LONG_PRESS_DURATION = 1000; // 1000 毫秒 = 1 秒

    public static void setupLongPress(JLabel label, JFrame parentFrame) {
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                longPressTimer = new Timer();
                longPressTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // 注意：Swing 组件更新需放到 EDT 线程中
                        SwingUtilities.invokeLater(() -> {
                            openDebugDialog(parentFrame);
                        });
                    }
                }, LONG_PRESS_DURATION);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                cancelTimer();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cancelTimer();
            }

            private void cancelTimer() {
                if (longPressTimer != null) {
                    longPressTimer.cancel();
                    longPressTimer = null;
                }
            }
        });
    }

    public static void openDebugDialog(JFrame parent) {
//        JOptionPane.showMessageDialog(parent, "长按触发调试窗口（DebugDialog）");
        // 或者你可以打开真正的 DebugDialog 类窗口
        DebugActivity dialog = new DebugActivity(parent);
         dialog.setVisible(true);
    }
}
