package com.bingo.sdk.web;

/**
 * Gson解析类,外层两个参数是通用的
 * 但是data下面每个接口都不一定相同,所以需要根据实际情况处理,这里用泛型处理
 *
 * @param <T>
 */
public class FormatResponse<T> {
    private int code = -1;
    private String msg;
    private T data;

    public int getCode() {
        return code;
    }

    public FormatResponse<T> setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public FormatResponse<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public T getData() {
        return data;
    }

    public FormatResponse<T> setData(T data) {
        this.data = data;
        return this;
    }


    @Override
    public String toString() {
        return "FormatResponse{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
