package com.bingo.sdk.callback;

public interface BingoSDKCallBack {

    /**
     * 初始化完成
     *
     * @param code code,参考{@link com.bingo.sdk.web.ApiStatusCode}
     * @param msg  msg
     */
    void onInitFinished(int code, String msg);

    /**
     * 登录完成
     *
     * @param code   code,参考{@link com.bingo.sdk.web.ApiStatusCode}
     * @param result 登录用户信息, Json格式
     */
    void onLoginFinished(int code, String result);

    /**
     * 切换账号完成
     * 返回内容与登录格式相同
     *
     * @param code   code,参考{@link com.bingo.sdk.web.ApiStatusCode}
     * @param result 登录用户信息, Json格式
     */
    void onChangeAccount(int code, String result);

    /**
     * 支付完成
     * 支付结果以后台通知为准
     *
     * @param code   code
     * @param result result
     */
    void onPayFinished(int code, String result);

    /**
     * 实名认证完成
     *
     * @param code
     */
    void onRealNameFinished(int code);

    /**
     * 退出登录(预留,目前没有地方调用)
     */
    void onLogout();

}
