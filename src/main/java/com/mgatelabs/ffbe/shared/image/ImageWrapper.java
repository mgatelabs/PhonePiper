package com.mgatelabs.ffbe.shared.image;

import com.mgatelabs.ffbe.shared.ColorSample;

import java.io.File;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/1/2017.
 */
public interface ImageWrapper {
    boolean isReady();

    int getWidth();

    int getHeight();

    int getPixel(int x, int y);

    void getPixel(int x, int y, ColorSample sample);

    boolean savePng(File file);
}
