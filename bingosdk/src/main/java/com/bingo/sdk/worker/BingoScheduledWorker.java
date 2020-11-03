package com.bingo.sdk.worker;

import com.bingo.sdk.inner.util.LogUtil;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BingoScheduledWorker {

    private ScheduledExecutorService mService;
    private ScheduledFuture<?> mFuture;

    public BingoScheduledWorker(int poolSize) {
        this.mService = new ScheduledThreadPoolExecutor(poolSize);
    }

    /**
     * @param runnable     runnable
     * @param initialDelay 首次延迟执行时间
     * @param period       间隔时间
     * @param unit         单位
     */
    public void invokeAtFixedRate(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
        if (mFuture != null) {
            LogUtil.e("队列已有定时任务,忽略当前添加");
            return;
        }

        this.mFuture = mService.scheduleAtFixedRate(runnable, initialDelay, period, unit);
    }

    public void cancel() {
        if (mFuture != null && !mFuture.isCancelled()) {
            mFuture.cancel(false);
            mFuture = null;
//            mService.shutdown();
//            mService = null;
        }
    }


}
