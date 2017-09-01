package com.mgatelabs.ffbe.shared;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/31/2017.
 */
public class RawImageReader {

    public enum ImageFormats {

        // RRGGBBAA
        // AARRGGBB

        RGBA(4, 0xFF000000, 8, 0xFF0000, 8, 0xFF00, 8, 0xFF, -24);

        ImageFormats(int bpp, int redMask, int redShift, int greenMask, int greenShift, int blueMask, int blueShift, int alphaMask, int alphaShift) {
            this.bpp = bpp;
            this.redMask = redMask;
            this.redShift = redShift;
            this.greenMask = greenMask;
            this.greenShift = greenShift;
            this.blueMask = blueMask;
            this.blueShift = blueShift;
            this.alphaMask = alphaMask;
            this.alphaShift = alphaShift;
        }

        private final int bpp;
        private final int redMask;
        private final int redShift;
        private final int greenMask;
        private final int greenShift;
        private final int blueMask;
        private final int blueShift;
        private final int alphaMask;
        private final int alphaShift;

        public int getBpp() {
            return bpp;
        }

        public int getRedMask() {
            return redMask;
        }

        public int getRedShift() {
            return redShift;
        }

        public int getGreenMask() {
            return greenMask;
        }

        public int getGreenShift() {
            return greenShift;
        }

        public int getBlueMask() {
            return blueMask;
        }

        public int getBlueShift() {
            return blueShift;
        }

        public int getAlphaMask() {
            return alphaMask;
        }

        public int getAlphaShift() {
            return alphaShift;
        }
    }

    private final int dataOffset;
    private final int width;
    private final int heigth;
    private final ImageFormats format;
    private final byte [] data;

    public RawImageReader(int width, int heigth, ImageFormats format, int dataOffset, byte[] data) {
        this.dataOffset = dataOffset;
        this.width = width;
        this.heigth = heigth;
        this.format = format;
        this.data = data;
    }

    public int getWidth() {
        return width;
    }

    public int getHeigth() {
        return heigth;
    }

    public ImageFormats getFormat() {
        return format;
    }

    public int getPixel(int x, int y) {
        int startIndex = dataOffset + ((y * width) * format.getBpp()) + (x * format.getBpp());

        int combined = ((0xff & data[startIndex]) << 24) | ((0xff & data[startIndex + 1]) << 16) | ((0xff & data[startIndex + 2]) << 8) | (0xff & data[startIndex + 3]);

        int r = (combined & format.getRedMask());
        if (format.getRedShift() > 0) {
            r >>= format.getRedShift();
        } else {
            r <<= (format.getRedShift() * -1);
        }

        int g = (combined & format.getGreenMask());
        if (format.getGreenShift() > 0) {
            g >>= format.getGreenShift();
        } else {
            g <<= (format.getGreenShift() * -1);
        }

        int b = (combined & format.getBlueMask());
        if (format.getBlueShift() > 0) {
            b >>= format.getBlueShift();
        } else {
            b <<= (format.getBlueShift() * -1);
        }

        int a = (combined & format.getAlphaMask());
        if (format.getAlphaShift() > 0) {
            a >>= format.getAlphaShift();
        } else {
            a <<= (format.getAlphaShift() * -1);
        }

        return a | r | g | b;
    }
}
