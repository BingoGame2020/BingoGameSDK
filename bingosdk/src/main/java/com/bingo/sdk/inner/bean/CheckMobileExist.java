package com.bingo.sdk.inner.bean;

public class CheckMobileExist {
    private boolean existMobile;


    public boolean isExistMobile() {
        return existMobile;
    }

    public CheckMobileExist setExistMobile(boolean existMobile) {
        this.existMobile = existMobile;
        return this;
    }

    @Override
    public String toString() {
        return "CheckMobileExist{" +
                "existMobile=" + existMobile +
                '}';
    }
}
