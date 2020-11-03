package com.bingo.sdk.string2pic.entity.block;

/**
 * IDAT数据块
 *
 * @author #Suyghur.
 * @date 2020/6/9
 *
 */
public class IDATBlock extends DataBlock {

    @Override
    public void setData(byte[] data) {
        this.data = data;
    }
}
