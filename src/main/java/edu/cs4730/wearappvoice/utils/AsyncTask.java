package edu.cs4730.wearappvoice.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;

public abstract class AsyncTask<Params, Progress, Result> {

    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    public final void execute(Params... params) {
        onPreExecute();
        THREAD_POOL.submit(() -> {
            Result result = doInBackground(params);
            SwingUtilities.invokeLater(() -> onPostExecute(result));
        });
    }

    /**
     * 如果你希望在任务开始前做准备（如显示加载框）
     */
    protected void onPreExecute() {
        // 默认空实现
    }

    /**
     * 子线程中执行耗时任务
     */
    protected abstract Result doInBackground(Params... params);

    /**
     * 后台任务完成后的处理（切回主线程）
     */
    protected void onPostExecute(Result result) {
        // 默认空实现
    }

    /**
     * 如果你希望中途更新进度（也会在主线程中回调）
     */
    protected void onProgressUpdate(Progress... values) {
        // 默认空实现
    }

    /**
     * 子线程中调用，用于进度更新
     */
    protected final void publishProgress(Progress... values) {
        SwingUtilities.invokeLater(() -> onProgressUpdate(values));
    }
}
