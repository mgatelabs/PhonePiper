package com.mgatelabs.ffbe.shared;

import java.awt.image.BufferedImage;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class SamplePoint {
    private final int x;
    private final int y;
    private final int r;
    private final int g;
    private final int b;

    public SamplePoint(int x, int y, int r, int g, int b) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.g = g;
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

    public static boolean validate(SamplePoint [] points, BufferedImage bufferedImage) {
        for (SamplePoint point: points) {
            int color = bufferedImage.getRGB(point.getX(), point.getY());
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
            if ( r >= 0 && r <= 4 && g >= 0 && g <= 4 && b >= 0 && b <= 4) {
                continue;
            }
            return false;
        }
        return true;
    }
}
