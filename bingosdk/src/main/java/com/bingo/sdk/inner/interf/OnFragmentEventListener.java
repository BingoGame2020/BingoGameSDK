package com.bingo.sdk.inner.interf;

import com.bingo.sdk.inner.annotation.FragmentEvent;

public interface OnFragmentEventListener {
    /**
     * fragment点击事件回调
     *
     * @param event   事件代码
     * @param isLogin 是否为登录界面(主要用于处理登录回调,因为登录的activity除了登录还有注册,当从注册取消时不应该走登录回调)
     * @param content 回调信息
     */
    void onEvent(@FragmentEvent int event, boolean isLogin, String content);
}