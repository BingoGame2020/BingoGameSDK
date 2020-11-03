package com.bingo.sdk.inner.interf;

import com.bingo.sdk.inner.bean.UserInfo;

public interface LoginCallBack {
    void onLogin(boolean success, String msg, UserInfo userInfo);
}
