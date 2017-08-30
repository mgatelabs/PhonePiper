package com.mgatelabs.ffbe.shared;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/29/2017.
 */
public class ColorSample {
    private int r;
    private int g;
    private int b;

    public ColorSample() {
    }

    public ColorSample(int srgb) {
        r = (srgb & 0xff0000) >> 16;
        g = (srgb & 0xff00) >> 8;
        b = srgb & 0xff;
    }

    public ColorSample(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public void parse(int srgb) {
        r = (srgb & 0xff0000) >> 16;
        g = (srgb & 0xff00) >> 8;
        b = srgb & 0xff;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }
}
