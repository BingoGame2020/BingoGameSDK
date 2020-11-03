package com.bingo.sdk.inner.bean;

public class PayOrder {
    private String payRecordNumber;

    public String getPayRecordNumber() {
        return payRecordNumber;
    }

    public PayOrder setPayRecordNumber(String payRecordNumber) {
        this.payRecordNumber = payRecordNumber;
        return this;
    }

    @Override
    public String toString() {
        return "PayOrder{" +
                "payRecordNumber='" + payRecordNumber + '\'' +
                '}';
    }
}
