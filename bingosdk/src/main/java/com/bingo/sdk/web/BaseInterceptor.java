package com.bingo.sdk.web;

import com.bingo.sdk.inner.util.AccountUtil;
import com.bingo.sdk.inner.util.CommonUtil;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class BaseInterceptor implements Interceptor {
    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        String token = CommonUtil.filterNull(AccountUtil.getToken());
//        LogUtil.e("intercept: 设置token: " + token);
        Request newRequest = request.newBuilder().addHeader("token", token).build();
        return chain.proceed(newRequest);
    }
}
