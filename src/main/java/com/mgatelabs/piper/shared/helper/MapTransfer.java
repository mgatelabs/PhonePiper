package com.mgatelabs.piper.shared.helper;

import com.mgatelabs.piper.shared.image.RawImageWrapper;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/7/2017.
 */
public class MapTransfer {
    private int columns;
    private int rows;
    private int width;
    private int bpp;

    private int startingOffset;
    private int nextRowOffset;
    private int rowSkip;

    private int blockSize;
    private int preSkip;
    private int postSkip;

    public void setup(int deviceWidth, int readOffset, int bpp, int areaWidth, int areaHeight, int areaX, int areaY, int tileWidth, int tileHeight) {
        this.width = deviceWidth;
        this.bpp = bpp;
        columns = areaWidth / tileWidth;
        rows = areaHeight / tileHeight;
        blockSize = tileWidth;

        rowSkip = 1;
        for (int i = 6; i >= 2; i--) {
            if (blockSize % i == 0) {
                rowSkip = i;
                break;
            }
        }

        preSkip = 0;
        postSkip = 1;
        startingOffset = RawImageWrapper.getOffsetFor(deviceWidth, readOffset, areaX, areaY, RawImageWrapper.ImageFormats.RGBA);
        nextRowOffset = (deviceWidth * bpp) - (areaWidth * bpp);
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public int getRowSkip() {
        return rowSkip;
    }

    public void setRowSkip(int rowSkip) {
        this.rowSkip = rowSkip;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getBpp() {
        return bpp;
    }

    public void setBpp(int bpp) {
        this.bpp = bpp;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getStartingOffset() {
        return startingOffset;
    }

    public void setStartingOffset(int startingOffset) {
        this.startingOffset = startingOffset;
    }

    public int getNextRowOffset() {
        return nextRowOffset;
    }

    public void setNextRowOffset(int nextRowOffset) {
        this.nextRowOffset = nextRowOffset;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public int getPreSkip() {
        return preSkip;
    }

    public void setPreSkip(int preSkip) {
        this.preSkip = preSkip;
    }

    public int getPostSkip() {
        return postSkip;
    }

    public void setPostSkip(int postSkip) {
        this.postSkip = postSkip;
    }
}
