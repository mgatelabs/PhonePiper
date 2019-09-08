package com.mgatelabs.piper.shared.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/1/2017 for Phone-Piper
 */
public class PngImageWrapper implements ImageWrapper {

    BufferedImage bufferedImage;

    public PngImageWrapper(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    public PngImageWrapper(byte[] data) {

        try {
            this.bufferedImage = ImageIO.read(new ByteArrayInputStream(data));
        } catch (Exception ex) {
            this.bufferedImage = null;
        }
    }

    @Override
    public boolean isReady() {
        return bufferedImage != null;
    }

    @Override
    public int getWidth() {
        return bufferedImage.getWidth();
    }

    @Override
    public int getHeight() {
        return bufferedImage.getHeight();
    }

    @Override
    public int getPixel(int x, int y) {
        return 0xFF000000 | bufferedImage.getRGB(x, y);
    }

    @Override
    public void getPixel(int x, int y, Sampler sample) {
        int color = bufferedImage.getRGB(x, y);
        sample.setR((color & 0xFF0000) >> 16);
        sample.setG((color & 0xFF00) >> 8);
        sample.setB((color & 0xFF));
    }

    @Override
    public boolean savePng(File file) {
        try {
            ImageIO.write(bufferedImage, "PNG", file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public byte[] outputPng() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "PNG", byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ImageWrapper getPngImage(File path) {
        if (!path.exists()) return null;
        try {
            final BufferedImage bufferedImage = ImageIO.read(path);
            return new PngImageWrapper(bufferedImage);
        } catch (Exception ex) {
            return null;
        }
    }

    public byte[] getRaw() {
        return new byte[0];
    }

    @Override
    public InputStream getInputStream() {
        return new FakeInputStream(bufferedImage);
    }

    public class FakeInputStream extends InputStream {

        private final BufferedImage bufferedImage;

        private int position;
        private int lastX;
        private int lastY;
        private int lastColor;

        public FakeInputStream(BufferedImage bufferedImage) {
            this.bufferedImage = bufferedImage;
            this.position = 0;
            this.lastX = -1;
            this.lastY = -1;
        }


        @Override
        public long skip(long n) throws IOException {
            position += n;
             return 0;
        }


        @Override
        public int read() throws IOException {
            // 4 bytes per pixel

            int current = position;
            position++;

            if (current < 12) {
                return 0;
            } else {

                int offset = current - 12;
                int diff = offset % 4;
                offset -= diff;
                int offsetPixel = offset / 4;

                int requestedY = offsetPixel / bufferedImage.getWidth();
                int requestedX = offsetPixel % bufferedImage.getWidth();

                if (requestedX != lastX || requestedY != lastY) {
                    lastX = requestedX;
                    lastY = requestedY;
                    lastColor = bufferedImage.getRGB(lastX, lastY);
                }

                switch (diff) {
                    case 0: {
                        return ((lastColor & 0xFF0000) >> 16);
                    }
                    case 1:
                        return ((lastColor & 0xFF00) >> 8);
                    case 2:
                        return ((lastColor & 0xFF));
                    default:
                        return 255;
                }
            }
        }
    }
}
