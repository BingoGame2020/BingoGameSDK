package com.bingo.sdk.string2pic.entity.block;

/**
 * 通用数据块
 *
 * @author #Suyghur.
 * @date 2020/6/9
 *
 */
public class CommonBlock extends DataBlock {
    @Override
    public void setData(byte[] data) {
        this.data = data;
    }
}
