package edu.cs4730.wearappvoice.utils;

import lombok.NonNull;

/**
 * 消息处理机制
 * <p>
 *     利用{@link Message}和{@link Looper}实现当前线程与其他线程之间的通信。
 *     主要实现其他线程消息的分发{@link #dispatchMessage(Message)}和处理{@link #handleMessage(Message)}
 * </p>
 */
public class Handler {
    //关联线程的Looper
    final Looper mLooper;
    //Looper的消息队列
    final MessageQueue mQueue;

    public Handler() {
        Looper looper = Looper.myLooper();
        if (looper == null) {
            throw new IllegalStateException("Cannot create Handler: Looper.prepare() must be called on this thread first.");
        }
        mLooper = looper;
        mQueue = looper.mQueue;
    }

    public Handler(Looper looper) {
        if (looper == null) {
            throw new IllegalArgumentException("Cannot create Handler with null Looper.");
        }
        mLooper = looper;
        mQueue = looper.mQueue;
    }
    
    public void Exit() {
        if (mLooper != null && mLooper.mThread != null) {
            mLooper.mThread.interrupt();
        }
    }

    public void handleMessage(Message msg) {
        System.out.println("Handler: handleMessage called.");
    }

    /**
     * 分发消息，两种处理方式
     * @param msg
     */
    public void dispatchMessage(Message msg) {
        if (msg.callBack != null) {
            msg.callBack.run();
        }else {
            handleMessage(msg);
        }
    }

    /**
     * 发送任务到当前线程
     * @param r
     */
    public final void post(Runnable r) {
        postDelayed(r,0);
    }

    /**
     * 延迟发送任务
     * @param r
     * @param delayMillis
     */
    public final void postDelayed(Runnable r, long delayMillis) {
        Message msg = Message.obtain(this, r);
        sendMessageDelayed(msg,delayMillis);
    }

    /**
     * 发送消息
     * @param msg
     */
    public final void sendMessage(Message msg) {
        sendMessageDelayed(msg,0);
    }

    /**
     * 延迟发送消息
     * @param msg
     * @param delayMillis
     */
    public final void sendMessageDelayed(Message msg, long delayMillis) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        sendMessageAtTime(msg,System.currentTimeMillis() + delayMillis);
    }

    /**
     * 在预定时刻发送消息
     * @param msg
     * @param uptimeMillis
     */
    public final void sendMessageAtTime(Message msg, long uptimeMillis) {
        if (mQueue == null) {
            throw new IllegalStateException("Cannot send message: MessageQueue is null.");
        }
        msg.target = this;
        mQueue.enqueueMessage(msg,uptimeMillis);
    }


    public final void removeCallbacks(@NonNull Runnable r) {
        mQueue.removeMessages(this, r, null);
    }

    public interface Callback {
        boolean handleMessage(@NonNull Message msg);
    }
}