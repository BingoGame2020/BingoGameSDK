package com.bingo.sdk.inner.bean;


import com.google.gson.Gson;

public class UserInfo {
    private String h5GameUrl;
    private String token;
    private String userName;
    private String userHeadUrl;
    private String password;
    private int isRealName;//是否实名: 0-未实名, 1-已实名
    private int isBind;//是否绑定手机: 0-未绑定, 1-已绑定
    private boolean forceRealName = false;

    public String getH5GameUrl() {
        return h5GameUrl;
    }

    public UserInfo setH5GameUrl(String h5GameUrl) {
        this.h5GameUrl = h5GameUrl;
        return this;
    }

    public String getToken() {
        return token;
    }

    public UserInfo setToken(String token) {
        this.token = token;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public UserInfo setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getUserHeadUrl() {
        return userHeadUrl;
    }

    public UserInfo setUserHeadUrl(String userHeadUrl) {
        this.userHeadUrl = userHeadUrl;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserInfo setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * 0-未实名, 1-已实名
     */
    public int getIsRealName() {
        return isRealName;
    }

    public UserInfo setIsRealName(int isRealName) {
        this.isRealName = isRealName;
        return this;
    }

    /**
     * 是否绑定手机: 0-未绑定, 1-已绑定
     */
    public int getIsBind() {
        return isBind;
    }

    public UserInfo setIsBind(int isBind) {
        this.isBind = isBind;
        return this;
    }

    public boolean isForceRealName() {
        return forceRealName;
    }

    public UserInfo setForceRealName(boolean forceRealName) {
        this.forceRealName = forceRealName;
        return this;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "h5GameUrl='" + h5GameUrl + '\'' +
                ", token='" + token + '\'' +
                ", userName='" + userName + '\'' +
                ", userHeadUrl='" + userHeadUrl + '\'' +
                ", password='" + password + '\'' +
                ", isRealName=" + isRealName +
                ", isBind=" + isBind +
                ", forceRealName=" + forceRealName +
                '}';
    }


    public String toJsonString() {
        return new Gson().toJson(this);
    }
}
