package com.mgatelabs.piper.shared.image;

import java.io.File;
import java.io.InputStream;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/1/2017 for Phone-Piper
 */
public interface ImageWrapper {
    boolean isReady();

    int getWidth();

    int getHeight();

    int getPixel(int x, int y);

    void getPixel(int x, int y, Sampler sample);

    boolean savePng(File file);

    byte [] outputPng();

    byte [] getRaw();

    InputStream getInputStream();
}
