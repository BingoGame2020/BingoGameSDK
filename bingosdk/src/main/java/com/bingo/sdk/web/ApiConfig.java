package com.bingo.sdk.web;

public class ApiConfig {
    public static final String URL_TEST = "http://test.qingin.cn";
    public static final String URL_PAY = "http://test.page.qingin.cn/#/pay/index";//h5支付地址
//    public static final String URL_PAY = "http://192.168.10.247:8080/#/pay/index";//h5支付地址

    public static final String HOST = URL_TEST;

    public static final String LOGIN_ACCOUNT = "/game-app-server-api/api/v1/account/login/account";
    /**
     * 游戏启动日志上报
     */
    public static final String GAME_START_REPORT = "/game-app-server-api/api/v1/sys/log/app/open";
    /**
     * 获取验证码
     */
    public static final String SEND_VERIFY_CODE = "/game-app-server-api/api/v1/msg/code/send/mobile";
    /**
     * 手机号快速注册
     */
    public static final String MOBILE_REGISTER = "/game-app-server-api/api/v1/account/register/mobile";
    /**
     * 账号快速注册
     */
    public static final String QUICK_REGISTER = "/game-app-server-api/api/v1/account/register/rapid";
    /**
     * 获取支付方式(渠道支付还是自己平台支付,自己平台支付才弹出支付框->再下单)
     */
    public static final String GET_PAYMENT = "/game-app-server-api/api/v1/pay/order/pre/create";
    /**
     * 创建订单
     */
    public static final String PAY_ORDER = "/game-app-server-api/api/v1/pay/order/create";
    /**
     * 微信网页支付
     */
    public static final String PAY_WECHAT_WEB = "/game-app-server-api/api/v1/pay/wechat/mobile/recharge";
    /**
     * 支付宝网页支付
     */
    public static final String PAY_ALIPAY_WEB = "/game-app-server-api/api/v1/pay/alipay/h5/recharge";
    /**
     * 获取支付结果
     */
    public static final String GET_PAY_RESULT = "/game-app-server-api/api/v1/pay/order/get/result";
    /**
     * 获取游戏配置
     */
    public static final String GET_GAME_CONFIG = "/game-app-server-api/api/v1/sys/game/config";
    /**
     * 手机号找回密码
     */
    public static final String FIND_PASSWORD_BY_PHONE = "/game-app-server-api/api/v1/account/user/update/password";
    /**
     * 角色时间打点
     */
    public static final String LOG_EVENT = "/game-app-server-api/api/v1/sys/log/save/event";
    /**
     * 应用更新
     */
    public static final String APP_UPDATE = "/game-app-server-api/api/v1/sys/version/check/android";
    /**
     * 手机绑定
     */
    public static final String BIND_PHONE = "/game-app-server-api/api/v1/account/user/bind/mobile";
    /**
     * 注销
     */
    public static final String LOG_OUT = "/game-app-server-api/api/v1/account/logout";
    /**
     * 在线心跳检测
     */
    public static final String LINE_HEART = "/game-app-server-api/api/v1/account/user/line_heart";
    /**
     * 实名认证
     */
    public static final String REAL_NAME = "/game-app-server-api/api/v1/account/user/realname/auth";
    /**
     * 检查手机号是否注册过
     */
    public static final String CHECK_MOBILE_EXISTS = "/game-app-server-api/api/v1/account/user/exist/mobile";
}
