package edu.cs4730.wearappvoice.utils;

import org.vosk.Model;
import java.util.ArrayList;
import java.util.List;

import edu.cs4730.wearappvoice.net.SendAudioTask;
import edu.cs4730.wearappvoice.voice.MySpeechService;
import edu.cs4730.wearappvoice.voice.request.ApiService;

public class Constants {
    public static String VOICE_SERVER = "https://carbot2.thanksalot.ren/carbot2/text";
    public static String VOICE_SERVER_UPLOAD = "https://carbot2.thanksalot.ren/carbot2/voice";
    public static String BASE_URL = "https://carbot2.thanksalot.ren/carbot2";
    public static String POSITION_UPLOAD_URL = "https://carbot2.thanksalot.ren/carbot2/set_location";  //上报位置信息
    public static int VOICE_PORT = 8999;

    public static String RECOGNIZE_URL = ""; // 人脸识别的web服务

    public static String MUSIC_SERVER = "146.56.230.120";
    public static int MUSIC_PORT = 28000;
    public static String WEATH_SERVER = "146.56.230.120";
    public static int WEATH_PORT = 8999;

    public static String AI_USERNAME = "1";
    public static String AI_SECRUITYKEY = "woshidalaohuang";
    public static String AI_TOKEN = "";
    public static TTS tts;
    public static Handler mainHandle;
    public static Handler debugHandle;
    public final static int MESSAGE_START_TTS  = 1000;
    public final static int MESSAGE_END_TTS  = 1001;
    public final static int MESSAGE_AI_VOICE_RECG  = 1002;
    public final static int MESSAGE_GOOGLE_VOICE_RECG  = 1003;
    public final static int MESSAGE_WAIT_AI_VOICE_RECG  = 1004;
    public final static int MESSAGE_VOICE_RECG_ERROR  = 1005;
    public final static int MESSAGE_SET_PLAY_VOICE  = 1006;
    public final static int MESSAGE_SHOW_VOICE_TEXT  = 1007;
    public final static int MESSAGE_UPDATE_MSGLIST  = 1008;
    public final static int MESSAGE_SHOW_LLM_VOICE_TEXT  = 1009;
    public final static int MESSAGE_STOP_LLM_RECG_LISTEN  = 1010;

    public static List<edu.cs4730.wearappvoice.voice.Message> dialogMessage = new ArrayList<>();   //记录对话过程的数据

    //AI助手的状态
    public final static String AI_NAME="小卡";
    public final static int AI_STATUS_SLEEP  = 0;
    public final static int AI_STATUS_IDEL  = 1;

    public static int AI_Status = AI_STATUS_IDEL;
    public static boolean ttsplaying = false;

    public static int isRecoByCloud = 2;
    public static boolean isDebug = false;

    public static String voicefile = "";  //用户的语音文件
    public static String qw_apikey = "请替换你申请的千问大模型KEY";  //千问的KEY

    public static ApiService mApiService = new ApiService();
    public static Model model;
    public static SendAudioTask mSendAudioTask;
    public static MySpeechService speechService;
    public static NotifyAudioPlayer notifyAudioPlayer;   //提示音播放类

    public static void sendTTSMessage(String str)
    {
        Message msg = new Message();
        msg.what = MESSAGE_START_TTS;
        msg.obj = str;
        if(mainHandle != null)
            mainHandle.sendMessage(msg);
    }

    /**
     * 将华氏温度转换为摄氏温度。
     *
     * @param fahrenheit 华氏温度
     * @return 对应的摄氏温度
     */
    public static double fahrenheitToCelsius(double fahrenheit) {
        double celsius = (fahrenheit - 32) * 5 / 9;
        return celsius;
    }

    /**
     * 将华氏温度转换为摄氏温度。
     *
     * @param fahrenheit 华氏温度
     * @return 对应的摄氏温度
     */
    public static double KelvinToCelsius(double fahrenheit) {
        return  Math.round((fahrenheit - 273.15) * 10.0) / 10.0;
    }
}
