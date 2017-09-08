package com.mgatelabs.ffbe.shared.image;

import java.io.File;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/1/2017.
 */
public interface ImageWrapper {
    boolean isReady();

    int getWidth();

    int getHeight();

    int getPixel(int x, int y);

    void getPixel(int x, int y, Sampler sample);

    boolean savePng(File file);
}
