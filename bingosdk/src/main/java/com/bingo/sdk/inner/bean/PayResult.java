package com.bingo.sdk.inner.bean;

public class PayResult {
    private String payRecordNumber;
    /**
     * 1：未支付；2：支付中；3：第三方处理中；4：支付成功；5：到账成功；6：支付超时；7：支付失败；8：关闭支付
     */
    private int status;

    public String getPayRecordNumber() {
        return payRecordNumber;
    }

    public PayResult setPayRecordNumber(String payRecordNumber) {
        this.payRecordNumber = payRecordNumber;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public PayResult setStatus(int status) {
        this.status = status;
        return this;
    }


    @Override
    public String toString() {
        return "PayResult{" +
                "payRecordNumber='" + payRecordNumber + '\'' +
                ", status=" + status +
                '}';
    }
}
