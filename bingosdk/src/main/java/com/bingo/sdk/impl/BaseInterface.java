package com.bingo.sdk.impl;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.bingo.sdk.bean.InitOptions;
import com.bingo.sdk.bean.RechargeOptions;
import com.bingo.sdk.bean.RoleEventOptions;
import com.bingo.sdk.bean.RoleEventType;
import com.bingo.sdk.callback.BingoExitCallBack;
import com.bingo.sdk.callback.BingoSDKCallBack;

public interface BaseInterface {
    /**
     * @param activity activity
     * @param opts     options SDK初始化的参数配置
     * @param callBack callback 初始化结果会通过onInit()返回
     */
    void init(Activity activity, InitOptions opts, BingoSDKCallBack callBack);

    void pay(Activity activity, RechargeOptions opts);

    void channelPay(Activity activity, RechargeOptions opts);

    void login(Activity activity);

    void autoLogin(Activity activity);

    void changeAccount(Activity activity);

    void logout(Activity activity);

    void onExit(Activity activity, BingoExitCallBack callBack);

    /**
     * 游戏启动
     * 在自定义application的onCreate调用
     */
    void onApplicationCreate(Application app);

    void onCreate(Activity activity);

    void onResume(Activity activity);

    void onPause(Activity activity);

    void onStart(Activity activity);

    void onRestart(Activity activity);

    void onNewIntent(Activity activity);

    void onStop(Activity activity);

    void onDestroy(Activity activity);

    void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data);

    void onRoleEvent(Activity activity, @RoleEventType int type, RoleEventOptions options);

    void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);

    boolean isLogin();

}
