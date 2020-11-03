package com.bingo.sdk.string2pic;

import com.bingo.sdk.inner.util.ByteUtils;
import com.bingo.sdk.string2pic.entity.Png;
import com.bingo.sdk.string2pic.entity.block.DataBlock;
import com.bingo.sdk.string2pic.entity.block.IDATBlock;
import com.bingo.sdk.string2pic.entity.block.IENDBlock;
import com.bingo.sdk.string2pic.entity.block.IHDRBlock;
import com.bingo.sdk.string2pic.entity.block.PHYSBlock;
import com.bingo.sdk.string2pic.entity.block.PLTEBlock;
import com.bingo.sdk.string2pic.entity.block.SRGBBlock;
import com.bingo.sdk.string2pic.entity.block.TEXTBlock;
import com.bingo.sdk.string2pic.entity.block.TRNSBlock;

import java.io.IOException;
import java.io.InputStream;


/**
 * @author #Suyghur.
 * @date 2020/6/9
 */
public class BlockFactory {

    public static DataBlock readBlock(InputStream in, Png png, DataBlock dataBlock) throws IOException {
        String hexCode = ByteUtils.byte2Hex(dataBlock.getChunkTypeCode(), 0, dataBlock.getChunkTypeCode().length);

        hexCode = hexCode.toUpperCase();
        DataBlock realDataBlock = null;
        if (BlockUtils.isIHDRBlock(hexCode)) {
            //IHDR数据块
            realDataBlock = new IHDRBlock();
        } else if (BlockUtils.isPLTEBlock(hexCode)) {
            //PLTE数据块
            realDataBlock = new PLTEBlock();
        } else if (BlockUtils.isIDATBlock(hexCode)) {
            //IDAT数据块
            realDataBlock = new IDATBlock();
        } else if (BlockUtils.isIENDBlock(hexCode)) {
            //IEND数据块
            realDataBlock = new IENDBlock();
        } else if (BlockUtils.isSRGBBlock(hexCode)) {
            //sRGB数据块
            realDataBlock = new SRGBBlock();
        } else if (BlockUtils.isTEXTBlock(hexCode)) {
            //tEXt数据块
            realDataBlock = new TEXTBlock();
        } else if (BlockUtils.isPHYSBlock(hexCode)) {
            //pHYs数据块
            realDataBlock = new PHYSBlock();
        } else if (BlockUtils.isTRNSBlock(hexCode)) {
            //tRNS数据块
            realDataBlock = new TRNSBlock();
        } else {
            //其它数据块
            realDataBlock = dataBlock;
        }

        realDataBlock.setLength(dataBlock.getLength());
        realDataBlock.setChunkTypeCode(dataBlock.getChunkTypeCode());
        int len = -1;
        byte[] data = new byte[8096];
        len = in.read(data, 0, ByteUtils.highByteToInt(dataBlock.getLength()));
        realDataBlock.setData(ByteUtils.cutByte(data, 0, len));
        return realDataBlock;
    }
}
