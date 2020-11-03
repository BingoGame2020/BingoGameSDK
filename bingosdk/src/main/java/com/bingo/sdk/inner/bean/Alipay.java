package com.bingo.sdk.inner.bean;

public class Alipay {
    private String form;
    private String payRecordNumber;


    public String getForm() {
        return form;
    }

    public Alipay setForm(String form) {
        this.form = form;
        return this;
    }

    public String getPayRecordNumber() {
        return payRecordNumber;
    }

    public Alipay setPayRecordNumber(String payRecordNumber) {
        this.payRecordNumber = payRecordNumber;
        return this;
    }

    @Override
    public String toString() {
        return "Alipay{" +
                "form='" + form + '\'' +
                ", payRecordNumber='" + payRecordNumber + '\'' +
                '}';
    }
}
