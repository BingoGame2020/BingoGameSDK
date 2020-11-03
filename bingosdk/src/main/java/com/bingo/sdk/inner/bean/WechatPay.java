package com.bingo.sdk.inner.bean;

public class WechatPay {
    private String mwebUrl;
    private String payRecordNumber;


    public String getMwebUrl() {
        return mwebUrl;
    }

    public WechatPay setMwebUrl(String mwebUrl) {
        this.mwebUrl = mwebUrl;
        return this;
    }

    public String getPayRecordNumber() {
        return payRecordNumber;
    }

    public WechatPay setPayRecordNumber(String payRecordNumber) {
        this.payRecordNumber = payRecordNumber;
        return this;
    }

    @Override
    public String toString() {
        return "WechatPay{" +
                "mwebUrl='" + mwebUrl + '\'' +
                ", payRecordNumber='" + payRecordNumber + '\'' +
                '}';
    }

}
