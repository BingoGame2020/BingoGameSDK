package com.bingo.sdk.inner.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 本地缓存账号
 */
public class Account {

    private int _id;
    private String uid;
    private String password;
    private String p_key;
    private String game;
    private String token;
    private String nickName;
    private boolean isRealName;
    private boolean isBindPhone;

    public int get_id() {
        return _id;
    }

    public Account set_id(int _id) {
        this._id = _id;
        return this;
    }

    public String getUid() {
        return uid;
    }

    public Account setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public Account setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getP_key() {
        return p_key;
    }

    public Account setP_key(String p_key) {
        this.p_key = p_key;
        return this;
    }


    public String getGame() {
        return game;
    }

    public Account setGame(String game) {
        this.game = game;
        return this;
    }

    public String getToken() {
        return token;
    }

    public Account setToken(String token) {
        this.token = token;
        return this;
    }

    public boolean isRealName() {
        return isRealName;
    }

    public Account setRealName(boolean realName) {
        isRealName = realName;
        return this;
    }

    public boolean isBindPhone() {
        return isBindPhone;
    }

    public Account setBindPhone(boolean bindPhone) {
        isBindPhone = bindPhone;
        return this;
    }

    public String getNickName() {
        return nickName;
    }

    public Account setNickName(String nickName) {
        this.nickName = nickName;
        return this;
    }

    @Override
    public String toString() {
        return "Account{" +
                "_id=" + _id +
                ", uid='" + uid + '\'' +
                ", password='" + password + '\'' +
                ", p_key='" + p_key + '\'' +
                ", game='" + game + '\'' +
                ", token='" + token + '\'' +
                ", nickName='" + nickName + '\'' +
                ", isRealName=" + isRealName +
                ", isBindPhone=" + isBindPhone +
                '}';
    }


    public String getCallBackString() {
        JSONObject json = new JSONObject();
        try {
            json.put("uid", uid);
            json.put("token", token);
            json.put("isRealName", isRealName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}
