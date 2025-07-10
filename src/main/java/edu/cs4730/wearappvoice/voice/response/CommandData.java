package edu.cs4730.wearappvoice.voice.response;

public class CommandData {
    public String input;
    public int code;
    public String cmd;
    public String musicName;
    public String contactName;
    public String destination;
    public Integer level;
    public String voiceUrl;
    public String voiceDataB64;
    public String textResponse; // For plain text responses

    public CommandData(String input, int code, String cmd, String musicName, String contactName, String destination, Integer level, String voiceUrl, String voiceDataB64, String textResponse) {
        this.input = input;
        this.code = code;
        this.cmd = cmd;
        this.musicName = musicName;
        this.contactName = contactName;
        this.destination = destination;
        this.level = level;
        this.voiceUrl = voiceUrl;
        this.voiceDataB64 = voiceDataB64;
        this.textResponse = textResponse;
    }
}

