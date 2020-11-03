package com.bingo.sdk.impl;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.bingo.sdk.inner.util.LogUtil;

public class WebViewJavaCallBack {
    private WebView webView;
    private OnWebViewListener webListener;
    private Handler handler;

    public WebViewJavaCallBack(WebView webView) {
        this.webView = webView;
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                int what = msg.what;
                String result = (String) msg.obj;
                if (webListener != null) {
                    webListener.onEvent(what, result);
                }
            }
        };
    }

    @JavascriptInterface
    public void send(int type, String msg) {
        LogUtil.e("get from webview :" + type + "\t" + msg);
        Message message = Message.obtain();
        message.what = type;
        message.obj = msg;
        handler.sendMessage(message);

    }

    public void release() {
        if (webView != null)
            webView = null;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    public void callJS(int type, String params) {
//        LogUtil.e("调用JS 参数:" + params);
        String js = "javascript:bingoSdk.setWebData(" + type + ",'" + params + "')";
        webView.evaluateJavascript(js, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
            }
        });
    }

    public void setOnWebViewListener(OnWebViewListener listener) {
        this.webListener = listener;
    }

    public interface OnWebViewListener {
        void onEvent(int type, String result);
    }
}
