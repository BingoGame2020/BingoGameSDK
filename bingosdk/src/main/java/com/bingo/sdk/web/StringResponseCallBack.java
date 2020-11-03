package com.bingo.sdk.web;

public interface StringResponseCallBack {
    void onSuccess(String result);

    void onFailed(int code, String result);
}
