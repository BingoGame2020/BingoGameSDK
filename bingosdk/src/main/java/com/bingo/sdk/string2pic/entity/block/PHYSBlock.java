package com.bingo.sdk.string2pic.entity.block;


import com.bingo.sdk.inner.util.ByteUtils;

/**
 * pHYs数据块
 *
 * @author #Suyghur.
 * @date 2020/6/9
 *
 */
public class PHYSBlock extends DataBlock {

    /**
     * Pixels per unit, X axis，4 bytes (PNG unsigned integer)
     */
    private byte[] xPixels;
    /**
     * Pixels per unit, Y axis，4 bytes (PNG unsigned integer)
     */
    private byte[] yPixels;
    /**
     * Unit specifier，1 byte
     */
    private byte[] unitSpecifier;

    public PHYSBlock() {
        super();
        xPixels = new byte[4];
        yPixels = new byte[4];
        unitSpecifier = new byte[1];
    }

    public byte[] getxPixels() {
        return xPixels;
    }

    public void setxPixels(byte[] xPixels) {
        this.xPixels = xPixels;
    }

    public byte[] getyPixels() {
        return yPixels;
    }

    public void setyPixels(byte[] yPixels) {
        this.yPixels = yPixels;
    }

    public byte[] getUnitSpecifier() {
        return unitSpecifier;
    }

    public void setUnitSpecifier(byte[] unitSpecifier) {
        this.unitSpecifier = unitSpecifier;
    }

    @Override
    public void setData(byte[] data) {
        int pos = 0;
        this.xPixels = ByteUtils.cutByte(data, pos, this.xPixels.length);
        pos += this.xPixels.length;
        this.yPixels = ByteUtils.cutByte(data, pos, this.yPixels.length);
        pos += this.yPixels.length;
        this.unitSpecifier = ByteUtils.cutByte(data, pos, this.unitSpecifier.length);
        pos += this.unitSpecifier.length;
        this.data = data;
    }
}
