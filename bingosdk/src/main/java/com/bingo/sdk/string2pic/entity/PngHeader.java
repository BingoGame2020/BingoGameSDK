package com.bingo.sdk.string2pic.entity;

/**
 * @author #Suyghur.
 * @date 2020/6/9
 */
public class PngHeader {
    /**
     * png文件头部信息，固定,8个字节
     */
    private byte[] flag;

    public PngHeader() {
        flag = new byte[8];
    }

    public byte[] getFlag() {
        return flag;
    }

    public void setFlag(byte[] flag) {
        this.flag = flag;
    }
}
