package edu.cs4730.wearappvoice.voice;
import edu.cs4730.wearappvoice.utils.*;


import edu.cs4730.wearappvoice.utils.Message;
import edu.cs4730.wearappvoice.voice.views.DataProcessView;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import edu.cs4730.wearappvoice.ai.OrderData.OrderResponseParser;
import edu.cs4730.wearappvoice.ai.poi.PoiSearchResult;
import edu.cs4730.wearappvoice.voice.response.CommandData;

import javax.swing.text.html.ListView;

/**
 * Service that records audio in a thread, passes it to a recognizer and emits
 * recognition results. Recognition events are passed to a client using
 * {@link RecognitionListener}
 */
public class commandProcess {
    private static final String TAG = "commandProcess";
    private Context mContext;

    public commandProcess(Context context, MessageAdapter _messageAdapter, List<Message> _messageList, ListView _messageListView) {
        super();
        mContext = context;
    }

    public CommandData handleJsonResponse(String jsonResponse) {
        try {
            jsonResponse = removeDuplicateNewLines(jsonResponse);
            JSONObject jsonObject = new JSONObject(jsonResponse);
            String input = jsonObject.getString("input");
            int code = jsonObject.getInt("code");
            String ttstext = jsonObject.getString("text");
            String cmddata = parseCommand(jsonResponse);
            String voiceUrl = jsonObject.isNull("voiceUrl") ? null : jsonObject.getString("voiceUrl");
            String voiceDataB64 = jsonObject.isNull("voiceDataB64") ? null : jsonObject.getString("voiceDataB64");

            String cmd = "";
            String musicName = null;
            String contactName = null;
            String destination = null;
            String text = null;
            Integer level = 0;
            String textResponse = null;

            try {
                //text = extractNestedJson(jsonResponse);
                JSONObject commandObject = null;
                if(cmddata != null) {
                    commandObject = new JSONObject(cmddata);
                    cmd = commandObject.getString("cmd");
                }

                switch (cmd) {
                    case "call":
                        try {
                            cmd += "-" + commandObject.getJSONObject("args").getString("action");
                        }
                        catch (Exception e)
                        {
                            contactName = commandObject.getJSONObject("args").getString("q");

                            //显示文字
                            text = "好的,给" + contactName + "打电话";
                            Message msg = new Message();
                            msg.what = Constants.MESSAGE_SHOW_VOICE_TEXT;
                            msg.obj = text;
                            Constants.mainHandle.sendMessage(msg);

                            String finalText = text;
                            String finalContactName = contactName;
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Constants.tts.startAnsyTTS(finalText); //播放完成后再拨打电话
                                    findContactAndMakeCall(finalContactName);
                                }
                            }).start();
                        }

                        break;
                    case "nav":
                        try {
                            destination = commandObject.getJSONObject("args").getString("to");
                            openNavigation(destination);
                        }
                        catch(Exception e)
                        {
                            openNavigation(null);
                        }
                        break;
                    case "nav_query":
                        destination = commandObject.getJSONObject("args").getString("q");
                        openNavigation(destination);
                        break;
                    case "navigate":
                        destination = commandObject.getJSONObject("args").getString("destination");
                        openNavigation(destination);
                        break;
                    case "play_music":
                        try {
                            musicName = commandObject.getJSONObject("args").getString("q");
                            if ((musicName != null) && (musicName.length() > 0)) {
                                playMusic(musicName);
                            }
                        }
                        catch (Exception e)
                        {
                            try {
                                int jump = commandObject.getJSONObject("args").getInt("jump");
                                musicName = jump + "";
                                break;
                            }
                            catch (Exception e1){
                            }

//                            Intent intent = new Intent(Intent.ACTION_VIEW);
//                            intent.setDataAndType(Uri.parse("content://media/internal/audio/media"), "audio/*");
//                            if (intent.resolveActivity(mContext.getPackageManager()) != null) {
//                                mContext.startActivity(intent);
//                            } else {
//                                Toast.makeText(mContext, "No music player found", Toast.LENGTH_LONG).show();
//                            }
                        }
                        break;

                    case "adjust_volume":
                        //textResponse = text.substring(text.indexOf("\n") + 1);
                    case "volume":
                    case "set_volume":
                        String vol = commandObject.getJSONObject("args").getString("value");
                        VolumeControl volumeControl = new VolumeControl(mContext);

                        try {
                            float volume = Float.parseFloat(vol);

                            if ((volume == -1) || (volume == 0)) {
                                // 设置为静音
                                volumeControl.setVolume(0);
                                textResponse = "好的，已关闭声音";
                            } else if (volume == 1) {
                                // 设置为最大音量
                                int maxVolume = volumeControl.getMaxVolume();
                                volumeControl.setVolume(maxVolume);
                                textResponse = "好的，已将音量调到最大";
                            } else if (volume == -0.1f) {
                                // 减小音量
                                volumeControl.decreaseVolume();
                                textResponse = "好的，已将音量调低";
                            } else if (volume == 0.1f) {
                                // 增加音量
                                volumeControl.increaseVolume();
                                textResponse = "好的，已将音量加大";
                            } else if (volume > 0.1f) {
                                // 设置为vol*100%*最大音量
                                int maxVolume = volumeControl.getMaxVolume();
                                int targetVolume = Math.round(volume * maxVolume);
                                volumeControl.setVolume(targetVolume);
                                textResponse = "好的，已调整音量";
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case "volume_up":
                        VolumeControl volumeControlUp = new VolumeControl(mContext);
                        volumeControlUp.increaseVolume();
                        level = -1;  // Indicate increase
                        break;
                    case "volume_down":
                        VolumeControl volumeControlDown = new VolumeControl(mContext);
                        volumeControlDown.decreaseVolume();
                        level = -2;  // Indicate decrease
                        break;
                    case "adjust_temperature":
                        level = commandObject.getJSONObject("args").getInt("value");
                        break;
                    case "adjust_windows":
                        destination = commandObject.getJSONObject("args").getString("action");
                        break;
                    case "get_orders":
                    case "get_cur_order": //显示当前运单
                    case "get_nth_order": //看第几个运单
                        textResponse = TransOrDisplayOrder(jsonResponse);
                        break;
                    case "get_queue_info"://排队
                        textResponse = TransOrDisplayOrder(jsonResponse);
                        break;
                    case "get_history_orders"://查询历史运单
                        textResponse = TransOrDisplayOrder(jsonResponse);
                        break;
                    case "show_order_detail":

                        break;
                    case "set_alarm": //设置闹钟
                        try {
                            String event = commandObject.getJSONObject("args").getString("event");
                            String time = commandObject.getJSONObject("args").getString("time");

                            AlarmClock alarmClock = new AlarmClock(mContext);
                            alarmClock.setAlarm(time, event);

                            destination = event;
                        }
                        catch (Exception e)
                        {
                        }
                        break;
                    case "search_nearby": //查询周边
                        PoiSearchResult res = new PoiSearchResult(jsonResponse);
                        textResponse = res.printSummary();

                        ShowPioView(res.getQuery(), res.getPoisString());
                        break;
                    case "get_concat": //查询装卸货联系人
                        //?????????
                        break;
                    case "get_wait_time": //查询装卸货等待时间
                        textResponse = ttstext;
                        String type = commandObject.getJSONObject("args").getString("type"); //装货/卸货等待时间
                        TransOrDisplayOrderWait(jsonResponse);
                        break;
                    default:
                        Log.e("JSON Response", "Unknown command: " + cmd);
                        textResponse = ttstext;
                        break;
                }
            } catch (JSONException e) {
                // The text is not a command JSON, treat it as a plain text response
                textResponse = ttstext;
            }

            return new CommandData(input, code, cmd, musicName, contactName, destination, level, voiceUrl, voiceDataB64, textResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void openNavigation(String destination) {

    }

    private void findContactAndMakeCall(String contact) {

    }

private String convertToPinyin(String chinese) {
    HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
    format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
    format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    format.setVCharType(HanyuPinyinVCharType.WITH_V);

    StringBuilder pinyin = new StringBuilder();
    char[] charArray = chinese.toCharArray();

    try {
        for (char c : charArray) {
            if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                if (pinyinArray != null) {
                    pinyin.append(pinyinArray[0]);
                }
            } else {
                pinyin.append(c);
            }
        }
    } catch (BadHanyuPinyinOutputFormatCombination e) {
        e.printStackTrace();
    }

//    Log.i("convertToPinyin ---", chinese + " --- " + pinyin.toString());
    return pinyin.toString();
}

    private void makePhoneCall(String phoneNumber) {

    }

    private void showContactChooser(List<Contact> contacts) {

    }


    private void showContactChooserDialog(List<Contact> contacts) {

    }


	private void playMusic(String musicName) {

    }

    private static class Contact {
        String name;
        String phoneNumber;

        Contact(String name, String phoneNumber) {
            this.name = name;
            this.phoneNumber = phoneNumber;
        }
    }

    //箭筒返回的数据中的cmd有时放在text中的问题
    public static String parseCommand(String jsonString) {
        try {
            // 将输入字符串解析为 JSONObject
            JSONObject jsonObject = new JSONObject(jsonString);

            // 尝试获取 cmd 字段
            if (!jsonObject.isNull("cmd")) {
                // 如果 cmd 不为 null，返回其 JSON 字符串
                return jsonObject.getJSONObject("cmd").toString();
            } else {
                // 如果 cmd 为 null，提取 text 并解析其中的 JSON
                String textContent = extractJsonFromText(jsonObject.getString("text"));
                if(textContent != null)
                {
                    return textContent;
                }
                else
                {
                    return null;
                }
                // 解析 text 中的嵌套 JSON
                //JSONObject textJsonObject = new JSONObject(textContent);

                // 返回嵌套 JSON 的字符串
                //return textJsonObject.toString();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null; // 或者可以选择返回一个错误信息
        }
    }

    // 辅助方法：提取有效的 JSON 字符串
    private static String extractJsonFromText(String textContent) {
        // 找到 JSON 对象的开始和结束位置
        int startIndex = textContent.indexOf('{');
        int endIndex = textContent.lastIndexOf('}');

        if (startIndex >= 0 && endIndex > startIndex) {
            // 提取 JSON 字符串，并去除可能的空白字符
            String jsonString = textContent.substring(startIndex, endIndex + 1).trim();
            return jsonString; // 返回有效的 JSON 字符串
        }

        return "{}"; // 如果没有找到，返回一个空的 JSON 对象
    }

    public static String extractNestedJson(String jsonString) {
        // 创建 JSONObject 对象
        JSONObject jsonObject = null;
        String nestedJson = jsonString;
        try {
            jsonObject = new JSONObject(jsonString);
            // 获取 "text" 字段内容
            String text = jsonObject.getString("text");

            // 清除 Markdown 格式的标记并提取 JSON 部分
            nestedJson = text.replace("```json\n", "").replace("\n```", "").trim();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return nestedJson; // 返回提取到的 JSON 字符串
    }

    /**
     * 去掉字符串中连续两个换行符，只保留一个换行符
     *
     * @param input 原始字符串
     * @return 处理后的字符串
     */
    public static String removeDuplicateNewLines(String input) {
        if (input == null) {
            return null; // 如果输入为null，直接返回null
        }
        // 使用正则表达式替换连续两个换行符为一个换行符
        return input.replaceAll("\n{2,}", "\n");
    }

//    public void testOrderDialog(String testOrder) {
//
//        Intent broadcastIntent = new Intent("AI_ORDER_UPDATE");
//        broadcastIntent.putExtra("order", testOrder);
//        mContext.sendBroadcast(broadcastIntent);
//
//        if(Constants.isDebug) {
//            Intent intent = new Intent(mContext, DataProcessView.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtra("order", testOrder);
//            mContext.startActivity(intent);
//
//            broadcastIntent = new Intent("AI_RESPONSE_SHOW_TEST");
//            broadcastIntent.putExtra("order", testOrder);
//            mContext.sendBroadcast(broadcastIntent);
//        }
//    }

    public void ShowPioView(String queryString, String poilistString) {
        if(Constants.isDebug) {
            Intent intent = new Intent(mContext, DataProcessView.class);
            intent.putExtra("poi_list", poilistString);
            intent.putExtra("query_type", queryString);
            mContext.startActivity(intent);
        }
    }

    public String TransOrDisplayOrder(String jsonResponse) {

        //发送数据给第三方APP
        Intent broadcastIntent = new Intent("AI_ORDER_UPDATE");
        broadcastIntent.putExtra("order", jsonResponse);
        if(mContext != null)
            mContext.sendBroadcast(broadcastIntent);

        Log.i("TransOrDisplayOrder", "发送广播AI_ORDER_UPDATE----" + jsonResponse);
        //调试状态展示数据
        if(Constants.isDebug) {
            Intent intent = new Intent(mContext, DataProcessView.class);
            intent.putExtra("order", jsonResponse);
            if(mContext != null)
                mContext.startActivity(intent);

            //发送此广播只是为了实时刷新数据展示页面
            broadcastIntent = new Intent("AI_RESPONSE_SHOW_TEST");
            broadcastIntent.putExtra("order", jsonResponse);
            if(mContext != null)
                mContext.sendBroadcast(broadcastIntent);
        }

        OrderResponseParser parser = new OrderResponseParser();
        if (parser.parseAndStore(jsonResponse)) {
            return parser.getResponseData().getCmd().getResult().msg;
        }

        return null;
    }

    public void TransOrDisplayOrderWait(String jsonResponse) {

        //发送装卸货等待时间数据给第三方APP
        Intent broadcastIntent = new Intent("AI_ORDER_UPDATE");
        broadcastIntent.putExtra("get_wait_time", jsonResponse);
        mContext.sendBroadcast(broadcastIntent);

        //调试状态展示数据
        if(Constants.isDebug) {

        }
    }
}
