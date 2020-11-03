package com.bingo.sdk.demo;

import android.app.Application;

import com.bingo.sdk.BingoSdkCore;

public class BaseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BingoSdkCore.getInstance().initApplication(this);
    }
}
