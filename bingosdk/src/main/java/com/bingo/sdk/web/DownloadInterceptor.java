package com.bingo.sdk.web;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class DownloadInterceptor implements Interceptor {
    private DownloadListener listener;

    public DownloadInterceptor( DownloadListener listener) {
        this.listener = listener;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        return response.newBuilder().body(new DownloadResponseBody(response,  listener)).build();
    }
}
