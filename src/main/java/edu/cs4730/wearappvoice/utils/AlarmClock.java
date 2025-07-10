package edu.cs4730.wearappvoice.utils;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


//闹钟类
public class AlarmClock {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final Map<Integer, ScheduledFuture<?>> alarms = new ConcurrentHashMap<>();
    private int alarmIdCounter = 0;
    private Context mContext;

    public AlarmClock(Context _c) {
        super();
        mContext = _c;
    }

    public synchronized void setAlarm(String time, String message) {
        try {
            SimpleDateFormat sdf;
            Date targetTime;

            if (time.matches("\\d{2}:\\d{2}")) { // 处理 "HH:mm" 格式
                sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                targetTime = sdf.parse(time);

                // 获取当前日期，并把解析的时间应用到当天
                Date currentDate = new Date();
                targetTime.setYear(currentDate.getYear());
                targetTime.setMonth(currentDate.getMonth());
                targetTime.setDate(currentDate.getDate());

            } else if (time.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{6}")) { // 处理 "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
                targetTime = sdf.parse(time);

            } else if (time.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) { // 处理 "yyyy-MM-dd'T'HH:mm:ss"
                sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss", Locale.getDefault());
                targetTime = sdf.parse(time);

            }
            else if (time.matches("\\d{2}:\\d{2}:\\d{2}")) { // 处理 "HH:mm:ss"
                sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                targetTime = sdf.parse(time);

                // 获取当前日期，并把解析的时间应用到当天
                Date currentDate = new Date();
                targetTime.setYear(currentDate.getYear());
                targetTime.setMonth(currentDate.getMonth());
                targetTime.setDate(currentDate.getDate());

            } else {
                System.out.println("时间格式错误，请使用 HH:mm、HH:mm:ss 或 yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
                return;
            }

            long delay = targetTime.getTime() - System.currentTimeMillis();
            if (delay <= 0) {
                System.out.println("设置失败：闹钟时间必须是未来的时间！");
                return;
            }

            scheduleAlarm(delay, message);
        } catch (Exception e) {
            System.out.println("时间解析错误：" + e.getMessage());
        }
    }


    public synchronized void setAlarm(int seconds, String message) {
        if (seconds <= 0) {
            System.out.println("设置失败：时间必须为正数！");
            return;
        }

        long delay = seconds * 1000L;
        scheduleAlarm(delay, message);
    }

    private void scheduleAlarm(long delay, String message) {
        int alarmId = ++alarmIdCounter;
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            System.out.println("🔔 闹钟响了：" + message);
            startRinging(alarmId, message);
        }, delay, TimeUnit.MILLISECONDS);

        alarms.put(alarmId, future);
    }

    private void startRinging1(int alarmId, String message) {
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            System.out.println("⏳ 闹钟结束：" + message);

            Constants.sendTTSMessage(message);

            alarms.remove(alarmId);
        }, 60, TimeUnit.SECONDS);

        alarms.put(alarmId, future);
    }

    private void startRinging(int alarmId, String message) {
        // ✅ 显示闹钟窗口
//        AlarmActivity.showlarm(mContext.getApplicationContext(), alarmId, message);

        // 30 秒后自动停止闹钟
        ScheduledFuture<?> future = scheduler.schedule(() -> stopAlarm(alarmId), 30, TimeUnit.SECONDS);
        alarms.put(alarmId, future);
    }

    // 停止闹钟
    private void stopAlarm(int alarmId) {
        if (alarms.containsKey(alarmId)) {
            alarms.get(alarmId).cancel(true);
            alarms.remove(alarmId);
        }
    }

    public void cancelAllAlarms() {
        for (ScheduledFuture<?> future : alarms.values()) {
            future.cancel(true);
        }
        alarms.clear();
        System.out.println("所有闹钟已取消");
    }

    public static void main(String[] args) {
        AlarmClock alarmClock = new AlarmClock(null);

        alarmClock.setAlarm("23:59:50", "睡觉时间到了！");
        alarmClock.setAlarm(10, "10秒后提醒！");
    }
}
