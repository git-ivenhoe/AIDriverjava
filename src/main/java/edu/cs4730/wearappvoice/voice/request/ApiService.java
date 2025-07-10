package edu.cs4730.wearappvoice.voice.request;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.cs4730.wearappvoice.utils.Constants;
import edu.cs4730.wearappvoice.utils.Message;

public class ApiService {
    private static ApiService instance;
    private static final String BASE_URL = Constants.BASE_URL;
    private String token;

    public ApiService() {
        // Private constructor to enforce singleton pattern
    }

    public static synchronized ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }

    public String login(String username, String securityKey) throws Exception {
        String loginUrl = BASE_URL + "/jwt/login?username=" + username + "&securityKey=" + securityKey;
        URL url = new URL(loginUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = ("").getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Constants.AI_TOKEN = "Bearer " + response.toString();
            System.out.println(" Constants.AI_TOKEN: " +  Constants.AI_TOKEN);
           return token;
        } else {
            throw new Exception("Login failed with response code: " + responseCode + " --- " + loginUrl);
        }
    }

    public String sendAndGetAIRequest(String requestUrl, String message) throws Exception {
        // Append the message parameter to the URL
        String encodedMessage = java.net.URLEncoder.encode(message, "UTF-8");
        String fullUrl = requestUrl + "?message=" + encodedMessage;

        URL url = new URL(fullUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set up the request
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Authorization", Constants.AI_TOKEN);
        // Check the response code
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) { // HTTP OK
            // Read the response
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        } else {
            throw new Exception("HTTP error code : " + responseCode + "---" + fullUrl);
        }
    }

    public void uploadWavData(byte[] wavData) {
        String urlStr = Constants.VOICE_SERVER_UPLOAD;
        String boundary = "===" + System.currentTimeMillis() + "===";

        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setRequestProperty("Authorization", Constants.AI_TOKEN);

            DataOutputStream request = new DataOutputStream(connection.getOutputStream());

            // Write WAV data
            request.writeBytes("--" + boundary + "\r\n");
            request.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"temp.wav\"\r\n");
            request.writeBytes("Content-Type: audio/wav\r\n\r\n");

            request.write(wavData);

            request.writeBytes("\r\n");
            request.writeBytes("--" + boundary + "--\r\n");
            request.flush();
            request.close();

            // Get response
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = responseReader.readLine()) != null) {
                response.append(line);
            }
            responseReader.close();

            Message msg = new Message();
            msg.what = Constants.MESSAGE_AI_VOICE_RECG;
            msg.obj = response.toString();
            Constants.mainHandle.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
