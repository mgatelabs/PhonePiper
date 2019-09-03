package com.mgatelabs.piper.shared.helper;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mgatelabs.piper.Runner;
import com.mgatelabs.piper.runners.ScriptRunner;
import com.mgatelabs.piper.shared.details.ConnectionDefinition;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.image.RawImageWrapper;
import com.mgatelabs.piper.shared.image.StateTransfer;
import com.mgatelabs.piper.shared.util.AdbUtils;
import com.mgatelabs.piper.shared.util.AdbWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.RemoteFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/21/2019 for Phone-Piper.
 */
public class LocalDeviceHelper implements DeviceHelper {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ConnectionDefinition connectionDefinition;
    private InfoTransfer info;
    private int failures;

    private File tempFile;

    public LocalDeviceHelper(ConnectionDefinition connectionDefinition) {
        this.connectionDefinition = connectionDefinition;
        try {
            this.tempFile = File.createTempFile("phonePiper", ".raw", Runner.WORKING_DIRECTORY);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
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

    private byte[] readTemp() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream(tempFile);
            byte[] temp = new byte[1024];
            int len = 0;
            while ((len = fileInputStream.read(temp, 0, 1024)) > 0) {
                byteArrayOutputStream.write(temp, 0, len);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new byte[0];
        } finally {
            Closer.close(byteArrayOutputStream);
            Closer.close(fileInputStream);
        }

        return byteArrayOutputStream.toByteArray();
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

        ByteArrayInputStream fileInputStream = null;
        boolean[] success = new boolean[stateTransfer.getScreenIds().size()];
        for (int i = 0; i < success.length; i++) {
            if (debugName.length() > 0 && stateTransfer.getScreenIds().get(i).equalsIgnoreCase(debugName)) {
                debugIndex = i;
            }
            success[i] = true;
        }

        try {
            fileInputStream = new ByteArrayInputStream(imageWrapper.getRaw());

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

        ByteArrayInputStream fileInputStream = null;

        try {
            fileInputStream = new ByteArrayInputStream(lastImageDownload);

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

        byte[] bytes = lastImageDownload;

        if (bytes == null) {
            bytes = new byte[0];
        }

        int w, h;
        if (bytes.length > 12) { // Sanity
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            w = byteBuffer.getInt();
            h = byteBuffer.getInt();
        } else {
            w = 0;
            h = 0;
        }
        return new RawImageWrapper(w, h, RawImageWrapper.ImageFormats.RGBA, 12, bytes);
    }

    @Override
    public int getFailures() {
        return failures;
    }

    byte[] lastImageDownload = null;

    @Override
    public boolean refresh(AdbWrapper shell) {

        long startTime = System.nanoTime();

        if (!AdbUtils.persistScreen(shell)) return false;

        JadbDevice device = shell.connect();

        if (device == null) return false;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            device.pull(new RemoteFile("/mnt/sdcard/framebuffer.raw"), byteArrayOutputStream);
            lastImageDownload = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (JadbException e) {
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
