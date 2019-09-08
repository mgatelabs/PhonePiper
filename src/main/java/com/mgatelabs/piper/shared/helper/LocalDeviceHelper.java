package com.mgatelabs.piper.shared.helper;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mgatelabs.piper.runners.ScriptRunner;
import com.mgatelabs.piper.shared.details.ConnectionDefinition;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.image.PngImageWrapper;
import com.mgatelabs.piper.shared.image.RawImageWrapper;
import com.mgatelabs.piper.shared.image.StateTransfer;
import com.mgatelabs.piper.shared.util.AdbUtils;
import com.mgatelabs.piper.shared.util.AdbWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.RemoteFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/21/2019 for Phone-Piper.
 */
public class LocalDeviceHelper implements DeviceHelper {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ConnectionDefinition connectionDefinition;
    private InfoTransfer info;
    private int failures;

    private boolean usePng;

    public LocalDeviceHelper(ConnectionDefinition connectionDefinition) {
        this.connectionDefinition = connectionDefinition;
    }

    public boolean isUsePng() {
        return usePng;
    }

    public void setUsePng(boolean usePng) {
        this.usePng = usePng;
    }

    @Override
    public boolean ready() {
        return true;
    }

    @Override
    public String getIpAddress() {
        return connectionDefinition.getIp();
    }

    @Override
    public void setConnectionDefinition(ConnectionDefinition connectionDefinition) {
        this.connectionDefinition = connectionDefinition;
    }

    @Override
    public boolean setup(InfoTransfer info) {
        this.info = info;
        return true;
    }

    @Override
    public Set<String> check(String menu) {

        ImageWrapper imageWrapper = download();

        if (imageWrapper == null) {
            failures++;
            return null;
        }

        int debugIndex = -1;
        String debugName = menu;

        StateTransfer stateTransfer = info.getStates().get(menu);

        if (stateTransfer == null) {
            return ImmutableSet.of();
        }

        InputStream fileInputStream = null;
        boolean[] success = new boolean[stateTransfer.getScreenIds().size()];
        for (int i = 0; i < success.length; i++) {
            if (debugName.length() > 0 && stateTransfer.getScreenIds().get(i).equalsIgnoreCase(debugName)) {
                debugIndex = i;
            }
            success[i] = true;
        }

        try {
            fileInputStream = imageWrapper.getInputStream();

            byte[] temp = new byte[3];
            int len;
            int remainingStates = success.length;
            boolean sampleRead = false;
            int extraRead = 0;

            for (PointTransfer pointTransfer : stateTransfer.getPoints()) {

                if (pointTransfer.getOffset() > 0) {
                    fileInputStream.skip(pointTransfer.getOffset() + extraRead);
                    sampleRead = false;
                    extraRead = SAMPLE_SIZE;

                }

                if (!success[pointTransfer.getIndex()]) continue;

                if (!sampleRead) {
                    len = fileInputStream.read(temp);
                    if (len != SAMPLE_SIZE) {
                        logger.error("Invalid byte read");
                        break;
                    }
                    sampleRead = true;
                    extraRead = 0;
                }

                success[pointTransfer.getIndex()] &= (within((0xff) & temp[0], (0xff) & pointTransfer.getA(), 6) && within((0xff) & temp[1], (0xff) & pointTransfer.getB(), 6) && within((0xff) & temp[2], (0xff) & pointTransfer.getC(), 6));

                if (!success[pointTransfer.getIndex()]) {
                    remainingStates--;
                }

                if (remainingStates <= 0) {
                    break;
                }

                if (pointTransfer.getIndex() == debugIndex) {
                    final boolean w1 = within((0xff) & temp[0], (0xff) & pointTransfer.getA(), 6);
                    final boolean w2 = within((0xff) & temp[1], (0xff) & pointTransfer.getB(), 6);
                    final boolean w3 = within((0xff) & temp[2], (0xff) & pointTransfer.getC(), 6);
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            failures++;
            return ImmutableSet.of();
        } finally {
            Closer.close(fileInputStream);
        }

        Set<String> successSet = Sets.newHashSet();
        for (int j = 0; j < success.length; j++) {
            if (success[j]) {
                successSet.add((stateTransfer.getScreenIds().get(j)));
            }
        }

        return successSet;
    }

    private static final int SAMPLE_SIZE = 3;

    public boolean within(int source, int test, int range) {
        int a = source - test;
        if (a < 0) a *= -1;
        return a <= range;
    }

    @Override
    public int[] pixel(int offset) {

        InputStream fileInputStream = null;

        try {
            fileInputStream = lastImageDownload.getInputStream();

            byte[] temp = new byte[3];
            int len;

            fileInputStream.skip(offset);
            len = fileInputStream.read(temp);
            if (len != 3) {
                throw new RuntimeException("Invalid byte read");
            }

            return new int[]{(0xff & temp[0]), (0xff & temp[1]), (0xff & temp[2])};

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new int[0];
        } finally {
            Closer.close(fileInputStream);
        }
    }

    @Override
    public ImageWrapper download() {
        return lastImageDownload;
    }

    @Override
    public int getFailures() {
        return failures;
    }

    //byte[] lastImageDownload = null;

    private ImageWrapper lastImageDownload;

    @Override
    public boolean refresh(AdbWrapper shell) {

        long startTime = System.nanoTime();

        if (!AdbUtils.persistScreen(shell, usePng)) return false;

        JadbDevice device = shell.getTargetedDevice();

        if (device == null) return false;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            device.pull(new RemoteFile("/mnt/sdcard/framebuffer." + (usePng ? "png" : "raw")), byteArrayOutputStream);
            byte[] temp = byteArrayOutputStream.toByteArray();
            if (temp != null) {
                if (usePng) {
                    lastImageDownload = new PngImageWrapper(temp);
                } else {
                    lastImageDownload = RawImageWrapper.convert(temp);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return false;
        } catch (JadbException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return false;
        }

        long endTime = System.nanoTime();
        long dif = endTime - startTime;

        float lastImageDuration = ((float) dif / 1000000000.0f);
        logger.debug("Helper Image Persisted in " + ScriptRunner.THREE_DECIMAL.format(lastImageDuration) + "s");

        return true;
    }
}
