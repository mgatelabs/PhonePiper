package com.mgatelabs.piper.shared.image;

import java.util.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class SamplePoint {
    private int x;
    private int y;
    private int r;
    private int g;
    private int b;

    public SamplePoint() {
    }

    public SamplePoint(int x, int y, int r, int g, int b) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setR(int r) {
        this.r = r;
    }

    public void setG(int g) {
        this.g = g;
    }

    public void setB(int b) {
        this.b = b;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public static boolean validate(final List<SamplePoint> points, final ImageWrapper imageWrapper, boolean log) {
        int i = 0;
        for (SamplePoint point: points) {
            int color = imageWrapper.getPixel(point.getX(), point.getY());
            int r = (color & 0xff0000) >> 16;
            int g = (color & 0xff00) >> 8;
            int b = color & 0xff;

            r -= point.r;
            g -= point.g;
            b -= point.b;

            if (r < 0) {
                r *= -1;
            }
            if (g < 0) {
                g *= -1;
            }
            if (b < 0) {
                b *= -1;
            }

            // Make it a bit fuzzy
            if ( r >= 0 && r <= 6 && g >= 0 && g <= 6 && b >= 0 && b <= 6) {
                i++;
                continue;
            }
            if (log) {
                System.out.println("Point: " + i + " Failed : " + r + ":" + g + ":" + b);
            }

            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "{" +
                "x=" + x +
                ", y=" + y +
                ", r=" + r +
                ", g=" + g +
                ", b=" + b +
                '}';
    }
}
