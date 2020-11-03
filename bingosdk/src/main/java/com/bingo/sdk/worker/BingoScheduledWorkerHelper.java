package com.bingo.sdk.worker;

import android.app.Activity;

import com.bingo.sdk.callback.BingoSDKCallBack;
import com.bingo.sdk.inner.bean.Account;
import com.bingo.sdk.inner.util.AccountUtil;
import com.bingo.sdk.inner.util.LogUtil;
import com.bingo.sdk.inner.util.ParameterChecker;
import com.bingo.sdk.web.HttpUtil;

import java.util.concurrent.TimeUnit;

public class BingoScheduledWorkerHelper {
    private static volatile BingoScheduledWorkerHelper instance;
    private BingoScheduledWorker mScheduledWorker;

    private BingoScheduledWorkerHelper() {
        if (mScheduledWorker == null) {
            mScheduledWorker = new BingoScheduledWorker(1);
        }
    }

    public static BingoScheduledWorkerHelper getInstance() {
        if (instance == null) {
            synchronized (BingoScheduledWorkerHelper.class) {
                if (instance == null) {
                    instance = new BingoScheduledWorkerHelper();
                }
            }
        }
        return instance;
    }

    public void cancelOnlineWorker() {
        if (mScheduledWorker != null) {
            mScheduledWorker.cancel();
        }
    }

    public void startRoleOnlineWorker(final Activity activity, final BingoSDKCallBack coreCallback) {
        ParameterChecker.checkActivity(activity);
        Account accountFromDb = AccountUtil.getCurrentLoginAccountFromDb(activity);
        if (accountFromDb == null) {
            LogUtil.e("数据库获取账号为空");
            return;
        }
        if (accountFromDb.isRealName()) {
            //实名认证的账号 不走心跳,后续可能会改
            LogUtil.e("账号已实名,不启动在线统计");
            return;
        }
        final int time = 1;//每次间隔时间,单位 分钟//todo 发布时改为3分钟
        mScheduledWorker.invokeAtFixedRate(new Runnable() {
            @Override
            public void run() {
                HttpUtil.syncPlayTime(activity, time, coreCallback);
            }
        }, time, time, TimeUnit.MINUTES);
    }
}
