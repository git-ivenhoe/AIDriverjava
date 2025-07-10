package edu.cs4730.wearappvoice.ai.OrderData;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Cmd {
    public String cmd;
    public Args args;
    public Result result;

    public String getCmd() {
        return cmd;
    }

    public Args getArgs() {
        return args;
    }

    public Result getResult() {
        return result;
    }
}
