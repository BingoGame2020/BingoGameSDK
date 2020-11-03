package com.bingo.sdk.web;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.bingo.sdk.inner.util.LogUtil;
import com.bingo.sdk.inner.util.ToastUtil;
import com.bingo.sdk.service.FileDownloadService;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BingoDownloadManager {
    private static final int TIME_OUT = 15;//超时时间15秒
    private static volatile BingoDownloadManager instance;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static BingoDownloadManager getInstance() {
        if (instance == null) {
            synchronized (BingoDownloadManager.class) {
                if (instance == null) {
                    instance = new BingoDownloadManager();
                }
            }
        }
        return instance;
    }


    public void download(final Context context, final String url) {
        if (TextUtils.isEmpty(url)) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showShortToast(context, "url为空");
                }
            });
            LogUtil.e("download url empty");
            return;
        }


//        FileDownloadService.setDownloadListener(listener);
        Intent intent = new Intent(context, FileDownloadService.class);
        intent.putExtra("url", url);
        context.startService(intent);

    }




}
