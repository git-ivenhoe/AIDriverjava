package edu.cs4730.wearappvoice.ai.OrderData;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

public class FullResponse {
    public String input;
    public int code;
    public String text;
    public Cmd cmd;
    public String voiceUrl;
    public String voiceDataB64;

    public String getInput() {
        return input;
    }

    public int getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public Cmd getCmd() {
        return cmd;
    }

    public String getVoiceUrl() {
        return voiceUrl;
    }

    public String getVoiceDataB64() {
        return voiceDataB64;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Args {
    public String from;
    public String to;
}

