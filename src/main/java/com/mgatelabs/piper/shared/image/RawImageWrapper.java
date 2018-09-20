package com.mgatelabs.piper.shared.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/31/2017.
 */
public class RawImageWrapper implements ImageWrapper {

    public enum ImageFormats {

        // RRGGBBAA
        // AARRGGBB

        RGBA(4, 0xFF000000, 8, 24, 0xFF0000, 8, 16, 0xFF00, 8, 8, 0xFF, -24);

        ImageFormats(int bpp, int redMask, int redShift, int redExtract, int greenMask, int greenShift, int greenExtract, int blueMask, int blueShift, int blueExtract, int alphaMask, int alphaShift) {
            this.bpp = bpp;
            this.redMask = redMask;
            this.redShift = redShift;
            this.redExtract = redExtract;
            this.greenMask = greenMask;
            this.greenShift = greenShift;
            this.greenExtract = greenExtract;
            this.blueMask = blueMask;
            this.blueShift = blueShift;
            this.blueExtract = blueExtract;
            this.alphaMask = alphaMask;
            this.alphaShift = alphaShift;
        }

        private final int bpp;
        private final int redMask;
        private final int redExtract;
        private final int redShift;
        private final int greenMask;
        private final int greenExtract;
        private final int greenShift;
        private final int blueMask;
        private final int blueExtract;
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

        public int getRedExtract() {
            return redExtract;
        }

        public int getGreenExtract() {
            return greenExtract;
        }

        public int getBlueExtract() {
            return blueExtract;
        }
    }

    private final int dataOffset;
    private final int width;
    private final int height;
    private final ImageFormats format;
    private final byte [] data;

    public RawImageWrapper(int width, int height, ImageFormats format, int dataOffset, byte[] data) {
        this.dataOffset = dataOffset;
        this.width = width;
        this.height = height;
        this.format = format;
        this.data = data;
    }

    @Override
    public boolean isReady() {
        if (width == 0 || height == 0) return false;
        return this.data.length >= this.dataOffset + ((width * height) * format.getBpp());
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public ImageFormats getFormat() {
        return format;
    }

    @Override
    public int getPixel(int x, int y) {
        int startIndex = dataOffset + ((y * width) * format.getBpp()) + (x * format.getBpp());

        if (startIndex + format.bpp > data.length) {
            return 0xFF000000;
        }

        int mergedBytes = ((0xff & data[startIndex]) << 24) | ((0xff & data[startIndex + 1]) << 16) | ((0xff & data[startIndex + 2]) << 8) | (0xff & data[startIndex + 3]);

        // Extract the colors
        final int r = shiftMe((mergedBytes & format.getRedMask()), format.getRedShift());
        final int g = shiftMe((mergedBytes & format.getGreenMask()), format.getGreenShift());
        final int b = shiftMe((mergedBytes & format.getBlueMask()), format.getBlueShift());
        final int a = shiftMe((mergedBytes & format.getAlphaMask()), format.getAlphaShift());

        // Return the color in the format ARGB
        return a | r | g | b;
    }

    @Override
    public void getPixel(int x, int y, Sampler sample) {
        int startIndex = dataOffset + ((y * width) * format.getBpp()) + (x * format.getBpp());
        sample.setR(0xff & data[startIndex]);
        sample.setG(0xff & data[startIndex + 1]);
        sample.setB(0xff & data[startIndex + 2]);
    }

    private int shiftMe(int value, int shift) {
        if (shift > 0) {
            value >>= shift;
        } else {
            value <<= (shift * -1);
        }
        return value;
    }

    @Override
    public boolean savePng(File file) {
        BufferedImage bufferedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = getPixel(x,y);
                bufferedImage.setRGB(x,y, 0xFFFFFF & color);
            }
        }
        try {
            ImageIO.write(bufferedImage, "PNG", file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public byte [] outputPng() {
        BufferedImage bufferedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = getPixel(x,y);
                bufferedImage.setRGB(x,y, 0xFFFFFF & color);
            }
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "PNG", byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getOffsetFor(final int width, final int dataOffset, final int x, final int y, final ImageFormats format) {
        return dataOffset + ((y * width) * format.getBpp()) + (x * format.getBpp());
    }
}
