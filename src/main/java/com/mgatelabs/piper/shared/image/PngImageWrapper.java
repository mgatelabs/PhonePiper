package com.mgatelabs.piper.shared.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/1/2017.
 */
public class PngImageWrapper implements ImageWrapper {

    BufferedImage bufferedImage;

    public PngImageWrapper(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
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
        return 0xFF000000 | bufferedImage.getRGB(x,y);
    }

    @Override
    public void getPixel(int x, int y, Sampler sample) {
        int color = bufferedImage.getRGB(x, y);
        sample.setR((color & 0xFF0000) >>16);
        sample.setG((color & 0xFF00) >>8);
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
    public byte [] outputPng() {
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
}
