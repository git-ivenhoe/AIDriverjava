package edu.cs4730.wearappvoice.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Log {
    private static final String LOG_TAG = "AppLogger";
    private static final String LOG_DIR_NAME = "logs";
    private static final String LOG_FILE_PREFIX = "app_log_";
    private static final String LOG_FILE_EXT = ".txt";
    private static File logFile;

    // 初始化日志文件（建议在 Application 中调用一次）
    public static void init() {

    }

    private static void writeToFile(String level, String tag, String message) {
        if(!Constants.isDebug)
        {
            return;
        }

        if (logFile == null) {
            Log.w(LOG_TAG, "Logger not initialized. Call AppLogger.init(context) first.");
            return;
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(new Date());
        String logLine = String.format("%s %s/%s: %s\n", timestamp, level, tag, message);

        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(logLine);
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    // 公开日志接口
    public static void d(String tag, String msg) {
        System.out.println(tag + ":" + msg);
        writeToFile("D", tag, msg);
    }

    public static void i(String tag, String msg) {
        System.out.println(tag + ":" + msg);
        writeToFile("I", tag, msg);
    }

    public static void w(String tag, String msg) {
        System.out.println(tag + ":" + msg);
        writeToFile("W", tag, msg);
    }

    public static void e(String tag, String msg) {
        System.out.println(tag + ":" + msg);
        writeToFile("E", tag, msg);
    }
}
