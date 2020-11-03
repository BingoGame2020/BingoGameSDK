package com.bingo.sdk.web;

import com.bingo.sdk.inner.util.LogUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

public class DownloadResponseBody extends ResponseBody {
    private Response response;
    private DownloadListener downloadListener;

    public DownloadResponseBody(Response response, DownloadListener downloadListener) {
        this.response = response;
        this.downloadListener = downloadListener;
    }

    @Override
    public long contentLength() {
        long length = response.body().contentLength();
        LogUtil.e("下载长度: " + length);
        return length;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        MediaType type = response.body().contentType();
        LogUtil.e("下载类型: " + type);
        return type;
    }

    @NotNull
    @Override
    public BufferedSource source() {
        return null;
    }
}
