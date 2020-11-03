package com.bingo.sdk.inner.consts;

public class PayResultStatus {
    /**
     * 未支付
     */
    public static final int UNPAID = 1;
    /**
     * 支付中
     */
    public static final int PAYING = 2;
    /**
     * 第三方处理中
     */
    public static final int THIRD_PARTY_PROCESSING = 3;
    /**
     * 支付成功
     */
    public static final int SUCCESSFUL = 4;
    /**
     * 到账成功
     */
    public static final int PAYMENT_RECEIVED = 5;
    /**
     * 支付超时
     */
    public static final int PAY_TIMEOUT = 6;
    /**
     * 支付失败
     */
    public static final int FAILED = 7;
    /**
     * 支付关闭
     */
    public static final int CLOSED = 8;


    public static String getCodeDesc(int code) {
        String desc = "查询失败";
        if (code == 1) {
            desc = "未支付";
        } else if (code == 2) {
            desc = "支付中";
        } else if (code == 3) {
            desc = "第三方处理中";
        } else if (code == 4) {
            desc = "支付成功";
        } else if (code == 5) {
            desc = "到账成功";
        } else if (code == 6) {
            desc = "支付超时";
        } else if (code == 7) {
            desc = "支付失败";
        } else if (code == 8) {
            desc = "关闭支付";
        }
        return desc;
    }
}
