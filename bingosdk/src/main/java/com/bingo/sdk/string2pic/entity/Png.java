package com.bingo.sdk.string2pic.entity;

import com.bingo.sdk.string2pic.entity.block.DataBlock;

import java.util.LinkedList;
import java.util.List;


/**
 * @author #Suyghur.
 * @date 2020/6/9
 */
public class Png {

    private PngHeader pngHeader;
    /**
     * 数据块集合
     */
    private List<DataBlock> dataBlocks;

    public Png() {
        dataBlocks = new LinkedList<>();
    }


    public PngHeader getPngHeader() {
        return pngHeader;
    }

    public void setPngHeader(PngHeader pngHeader) {
        this.pngHeader = pngHeader;
    }

    public List<DataBlock> getDataBlocks() {
        return dataBlocks;
    }

    public void setDataBlocks(List<DataBlock> dataBlocks) {
        this.dataBlocks = dataBlocks;
    }
}
