package com.bingo.sdk.inner.bean;

import java.util.List;

public class Game {
    private int code;    //0为操作成功的状态码
    private String msg;//	状态码信息
    private int forceRealName = 2;//	是否强制实名, 1:是   2:否
    private int realNameType;    //	1:关闭提示 2:当天首次登录提示 3:每次登录提示
    private int bindMobileType;    //	1:关闭提示 2:当天首次登录提示 3:每次登录提示
    private int cutLoginStatus;    //	是否切登录 1：是(走我们平台登录) 2：否
    private int floatWindowsStatus;//	浮窗状态 1:显示 2：隐藏
    private String customerUrl;//客服地址,支付失败,或者注册等地方的跳转
    private String privacyUrl;//隐私协议地址
    private List<FloatWindow> floatWindowsVoList;//	floatWindowsStatus=2的时候整个浮窗不显示

    public int getCode() {
        return code;
    }

    public Game setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public Game setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    /**
     * 1:关闭提示 2:当天首次登录提示 3:每次登录提示
     */
    public int getRealNameType() {
        return realNameType;
    }

    public Game setRealNameType(int realNameType) {
        this.realNameType = realNameType;
        return this;
    }

    public int getBindMobileType() {
        return bindMobileType;
    }

    public Game setBindMobileType(int bindMobileType) {
        this.bindMobileType = bindMobileType;
        return this;
    }

    public int getCutLoginStatus() {
        return cutLoginStatus;
    }

    public Game setCutLoginStatus(int cutLoginStatus) {
        this.cutLoginStatus = cutLoginStatus;
        return this;
    }

    public int getFloatWindowsStatus() {
        return floatWindowsStatus;
    }

    public Game setFloatWindowsStatus(int floatWindowsStatus) {
        this.floatWindowsStatus = floatWindowsStatus;
        return this;
    }

    public List<FloatWindow> getFloatWindowsVoList() {
        return floatWindowsVoList;
    }

    public Game setFloatWindowsVoList(List<FloatWindow> floatWindows) {
        this.floatWindowsVoList = floatWindows;
        return this;
    }

    /**
     * 是否强制实名, 1:是   2:否
     *
     * @return
     */
    public int getForceRealName() {
        return forceRealName;
    }

    public Game setForceRealName(int forceRealName) {
        this.forceRealName = forceRealName;
        return this;
    }


    public String getCustomerUrl() {
        return customerUrl;
    }

    public Game setCustomerUrl(String customerUrl) {
        this.customerUrl = customerUrl;
        return this;
    }

    public String getPrivacyUrl() {
        return privacyUrl;
    }

    public Game setPrivacyUrl(String privacyUrl) {
        this.privacyUrl = privacyUrl;
        return this;
    }

    @Override
    public String toString() {
        return "Game{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", forceRealName=" + forceRealName +
                ", realNameType=" + realNameType +
                ", bindMobileType=" + bindMobileType +
                ", cutLoginStatus=" + cutLoginStatus +
                ", floatWindowsStatus=" + floatWindowsStatus +
                ", customerUrl='" + customerUrl + '\'' +
                ", privacyUrl='" + privacyUrl + '\'' +
                ", floatWindowsVoList=" + floatWindowsVoList +
                '}';
    }
}
