package com.bingo.sdk.inner.bean;

import android.net.Uri;

import java.io.File;

public class DownloadUri {
    /**
     * 仅供Q+使用
     */
    private Uri uri;
    //用来设置不同的notification,分包更新进度
    private int contentId;
    /**
     * Q以下
     */
    private File file;

    public File getFile() {
        return file;
    }

    public DownloadUri setFile(File file) {
        this.file = file;
        return this;
    }

    public Uri getUri() {
        return uri;
    }

    public DownloadUri setUri(Uri uri) {
        this.uri = uri;
        return this;
    }

    public int getContentId() {
        return contentId;
    }

    public DownloadUri setContentId(int contentId) {
        this.contentId = contentId;
        return this;
    }


}
