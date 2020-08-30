package com.mgatelabs.piper.shared.helper;

import com.google.common.collect.ImmutableSet;
import com.mgatelabs.piper.shared.details.ConnectionDefinition;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.util.AdbWrapper;

import java.util.Set;

/**
 * Created by @mgatelabs (Michael Fuller) on 11/25/2019 for Phone-Piper.
 */
public class NoOpDeviceHelper implements DeviceHelper {
    @Override
    public boolean ready() {
        return true;
    }

    @Override
    public String getIpAddress() {
        return "127.0.0.1";
    }

    @Override
    public void setConnectionDefinition(ConnectionDefinition connectionDefinition) {

    }

    @Override
    public boolean setup(InfoTransfer info) {
        return true;
    }

    @Override
    public Set<String> check(String menu) {
        return ImmutableSet.of();
    }

    @Override
    public int[] pixel(int offset) {
        return new int[0];
    }

    @Override
    public ImageWrapper download() {
        return null;
    }

    @Override
    public int getFailures() {
        return 0;
    }

    @Override
    public RefreshReceipt refresh(AdbWrapper shell, int screenIndex) {
        return new RefreshReceipt(0, System.currentTimeMillis(), true);
    }

    @Override
    public DeviceHelper makeReady(AdbWrapper shell) {
        return this;
    }

    @Override
    public boolean imageReady() {
        return false;
    }

    @Override
    public long getImageAverage() {
        return 0;
    }

    @Override
    public long getImageSamples() {
        return 0;
    }

    @Override
    public long getLastImageTime() {
        return 0;
    }
}
