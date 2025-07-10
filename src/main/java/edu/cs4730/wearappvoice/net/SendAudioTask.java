package edu.cs4730.wearappvoice.net;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.cs4730.wearappvoice.utils.AsyncTask;
import edu.cs4730.wearappvoice.utils.Constants;
import edu.cs4730.wearappvoice.utils.Context;
import edu.cs4730.wearappvoice.utils.Log;

public class SendAudioTask {
    private Context context;
    private String TAG = "SendAudioTask";
    private String server_addr;
    private int port;
    private byte[] audioData;

    public interface UploadCallback {
        void onSuccess(String response);
        void onFailure(String errorMessage);
    }

    public SendAudioTask(Context context, String server_addr, int port) {
        this.context = context;
        this.server_addr = server_addr;
        this.port = port;
    }

    public void setAudioData(byte[] audioData) {
        this.audioData = audioData;
    }

    HttpURLConnection urlConnection = null;
    public void uploadAudioAsync(final UploadCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {

                DataOutputStream outputStream = null;
                try {
                    URL url = new URL("http://" + server_addr + ":" + port + "/speech");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "audio/wav");
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.connect();

                    outputStream = new DataOutputStream(new BufferedOutputStream(urlConnection.getOutputStream()));
                    outputStream.write(audioData, 0, audioData.length);
                    outputStream.flush();

                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Read and return response from server
                        // 获取响应流
                        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();

                        // 读取响应内容
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }

                        // 关闭响应流
                        in.close();
                        // 打印响应内容
                        System.out.println("识别结果：" + response.toString());

                        callback.onSuccess(response.toString());
                    } else {
                        callback.onFailure("HTTP请求失败，错误码: " + responseCode);
                        Log.e(TAG, "HTTP请求失败，错误码: " + responseCode);
                        return null;
                    }
                } catch (IOException e) {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error sending audio data: " + e.getMessage());
                    return null;
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
                return null;
            }


            @Override
            protected void onPostExecute(String result) {
//                if (result != null) {
//                    Toast.makeText(context, "识别结果: " + result, Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(context, "无法识别语音", Toast.LENGTH_SHORT).show();
//                }
            }
        }.execute();
    }

    public void getCommandRtn(String message, final UploadCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String result = null;
                try {
                    result = Constants.mApiService.sendAndGetAIRequest(server_addr, message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onFailure("抱歉，服务异常");
                }
            }
        }.execute();
    }
}