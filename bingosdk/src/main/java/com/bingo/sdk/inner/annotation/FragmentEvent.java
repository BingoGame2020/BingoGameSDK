package com.bingo.sdk.inner.annotation;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@IntDef({FragmentEvent.EVENT_CLOSE, FragmentEvent.EVENT_BACK, FragmentEvent.EVENT_NEXT, FragmentEvent.EVENT_QUICK_GAME
        , FragmentEvent.EVENT_ACCOUNT_LOGIN, FragmentEvent.EVENT_LOGIN_SUCCESS, FragmentEvent.EVENT_PAY_CANCEL
        , FragmentEvent.EVENT_PAY_SUCCESSFUL, FragmentEvent.EVENT_TO_REGISTER, FragmentEvent.EVENT_TO_FORGET_PWD
        , FragmentEvent.EVENT_RESET_PWD_SUCCESS, FragmentEvent.EVENT_PAY_FAILED, FragmentEvent.EVENT_CHANGE_ACCOUNT
        , FragmentEvent.EVENT_LOGIN_FAILED, FragmentEvent.EVENT_QUICK_LOGIN_SUCCESS, FragmentEvent.EVENT_REGISTER
}
)


@Retention(RetentionPolicy.SOURCE)
public @interface FragmentEvent {
    int EVENT_CLOSE = 0;//fragment关闭按钮
    int EVENT_BACK = 1;//返回按钮
    int EVENT_NEXT = 2;//登录界面下一步
    int EVENT_QUICK_GAME = 3;//登录界面快速游戏
    int EVENT_ACCOUNT_LOGIN = 4;//登录界面->账号登录
    int EVENT_LOGIN_SUCCESS = 5;//登录成功
    int EVENT_PAY_CANCEL = 6;//取消支付
    int EVENT_PAY_SUCCESSFUL = 7;//支付成功
    int EVENT_TO_REGISTER = 8;//注册
    int EVENT_TO_FORGET_PWD = 9;//忘记密码
    int EVENT_RESET_PWD_SUCCESS = 10;//重置密码成功
    int EVENT_PAY_FAILED = 11;//支付失败
    int EVENT_CHANGE_ACCOUNT = 12;//切换账号
    int EVENT_LOGIN_FAILED = 13;//登录失败
    int EVENT_QUICK_LOGIN_SUCCESS = 14;//快速游戏登录成功
    int EVENT_REGISTER = 15;//快速游戏登录成功
}
