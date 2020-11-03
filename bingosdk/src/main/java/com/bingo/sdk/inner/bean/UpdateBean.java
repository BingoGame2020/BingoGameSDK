package com.bingo.sdk.inner.bean;

import android.text.TextUtils;

public class UpdateBean {
    private String versionName;
    private String versionDesc;
    private String downloadUrl;
    private boolean forceUpdate;

    public String getVersionName() {
        return versionName;
    }

    public UpdateBean setVersionName(String versionName) {
        this.versionName = versionName;
        return this;
    }

    public String getVersionDesc() {
        return versionDesc;
    }

    public UpdateBean setVersionDesc(String versionDesc) {
        this.versionDesc = versionDesc;
        return this;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public UpdateBean setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        return this;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public UpdateBean setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
        return this;
    }

    @Override
    public String toString() {
        return "UpdateBean{" +
                "versionName='" + versionName + '\'' +
                ", versionDesc='" + versionDesc + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", forceUpdate=" + forceUpdate +
                '}';
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(downloadUrl);
    }
}
