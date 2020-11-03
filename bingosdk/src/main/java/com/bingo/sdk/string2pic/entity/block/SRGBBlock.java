package com.bingo.sdk.string2pic.entity.block;


import com.bingo.sdk.inner.util.ByteUtils;

/**
 * sRGB数据块
 *
 * @author #Suyghur.
 * @date 2020/6/9
 *
 */
public class SRGBBlock extends DataBlock {

    /**
     * Rendering intent,1个字节
     */
    private byte[] renderingIntent;

    public SRGBBlock() {
        super();
        renderingIntent = new byte[1];
    }

    public byte[] getRenderingIntent() {
        return renderingIntent;
    }

    public void setRenderingIntent(byte[] renderingIntent) {
        this.renderingIntent = renderingIntent;
    }

    @Override
    public void setData(byte[] data) {
        int pos = 0;
        this.renderingIntent = ByteUtils.cutByte(data, pos, this.renderingIntent.length);
        pos += this.renderingIntent.length;

        this.data = data;
    }
}
