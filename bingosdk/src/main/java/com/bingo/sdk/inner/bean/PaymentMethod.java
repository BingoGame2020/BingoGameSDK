package com.bingo.sdk.inner.bean;

public class PaymentMethod {
    private int cutPayStatus;

    /**
     * 是否走自己平台支付(H5)
     *
     * @return true->是   false->否
     */
    public boolean isPlatformPay() {
        return cutPayStatus == 1;
    }

    public PaymentMethod setCutPayStatus(int cutPayStatus) {
        this.cutPayStatus = cutPayStatus;
        return this;
    }


    @Override
    public String toString() {
        return "PaymentMethod{" +
                "isPlatformPay=" + isPlatformPay() +
                '}';
    }
}
