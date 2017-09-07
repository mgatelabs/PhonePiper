package com.mgatelabs.ffbe.shared.image;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/6/2017.
 */
public class PointTransfer {

    int offset;
    byte index;
    byte a;
    byte b;
    byte c;

    public PointTransfer() {
    }

    public PointTransfer(int offset, byte index, byte a, byte b, byte c) {
        this.offset = offset;
        this.index = index;
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public byte getIndex() {
        return index;
    }

    public void setIndex(byte index) {
        this.index = index;
    }

    public byte getA() {
        return a;
    }

    public void setA(byte a) {
        this.a = a;
    }

    public byte getB() {
        return b;
    }

    public void setB(byte b) {
        this.b = b;
    }

    public byte getC() {
        return c;
    }

    public void setC(byte c) {
        this.c = c;
    }
}
