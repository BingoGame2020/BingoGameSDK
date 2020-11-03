package com.bingo.sdk.web;

public interface DownloadListener {
    void onLoading(long progress, long maxLength);

    void onComplete();

    void onFailed(int code, String msg);

}
