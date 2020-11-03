package com.bingo.sdk.inner.util;

import android.app.Activity;

import com.bingo.sdk.callback.BingoSDKCallBack;
import com.bingo.sdk.impl.BaseInterface;

public class ParameterChecker {

    public static void checkActivity(Activity activity) {
        if (activity == null) {
            throw new IllegalArgumentException("Activity can not be null");
        }
    }

    public static void checkImpl(BaseInterface impl) {
        if (impl == null) {
            throw new RuntimeException("initApplication()未调用,或者channel_id有误");
        }
    }

    public static void checkCallBack(BingoSDKCallBack coreCallback) {
        if (coreCallback == null)
            throw new IllegalArgumentException("BingoSDKCallBack为空,请检查是否调用InitSdk()并传入callback");
    }
}
