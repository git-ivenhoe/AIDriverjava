package edu.cs4730.wearappvoice.voice.request;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.cs4730.wearappvoice.utils.Constants;

public class LoginScheduler {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void startLoginTask() {
        Runnable loginTask = () -> {
            try {
                Constants.mApiService.login(Constants.AI_USERNAME, Constants.AI_SECRUITYKEY);
                System.out.println("Login executed at: " + System.currentTimeMillis());
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        // 第一次立即执行，之后每12小时执行一次
        scheduler.scheduleAtFixedRate(loginTask, 0, 12, TimeUnit.HOURS);
    }
}
