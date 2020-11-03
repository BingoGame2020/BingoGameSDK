package com.bingo.sdk.inner.bean;

/**
 * 公告浮窗
 */
public class FloatWindow {
    private String name;
    private int type;
    private String  h5Url;

    public String getName() {
        return name;
    }

    public FloatWindow setName(String name) {
        this.name = name;
        return this;
    }

    public int getType() {
        return type;
    }

    public FloatWindow setType(int type) {
        this.type = type;
        return this;
    }

    public String getH5Url() {
        return h5Url;
    }

    public FloatWindow setH5Url(String h5Url) {
        this.h5Url = h5Url;
        return this;
    }

    @Override
    public String toString() {
        return "FloatWindow{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", h5Url=" + h5Url +
                '}';
    }
}
