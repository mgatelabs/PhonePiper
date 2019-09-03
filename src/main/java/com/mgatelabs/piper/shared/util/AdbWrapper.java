package com.mgatelabs.piper.shared.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.vidstige.jadb.ConnectionToRemoteDeviceException;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/2/2019 for Phone-Piper.
 */
public class AdbWrapper {

    private static final Logger logger = LoggerFactory.getLogger(AdbWrapper.class);

    enum AdbWrapperStatus {
        INIT,
        READY,
        FAILED
    }

    private final JadbConnection connection;
    private final InetSocketAddress address;
    private AdbWrapperStatus connectionStatus;

    public AdbWrapper(final String path, final int port) {
        batch = Lists.newArrayList();
        connectionStatus = AdbWrapperStatus.INIT;
        connection = new JadbConnection();
        address = InetSocketAddress.createUnresolved(path, port);
        connect();
    }

    public void shutdown() {

    }

    public JadbDevice connect() {
        JadbDevice device = getDevice();
        try {
            if (device != null && device.getState() == JadbDevice.State.Device) {
                logger.trace("AdbWrapper: Re-using connection");
                return device;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            connection.connectToTcpDevice(address);
            connectionStatus = AdbWrapperStatus.READY;
            return getDevice();
        } catch (IOException e) {
            e.printStackTrace();
            connectionStatus = AdbWrapperStatus.FAILED;
        } catch (JadbException e) {
            e.printStackTrace();
            connectionStatus = AdbWrapperStatus.FAILED;
        } catch (ConnectionToRemoteDeviceException e) {
            e.printStackTrace();
            connectionStatus = AdbWrapperStatus.FAILED;
        }
        return null;
    }

    public String status() {

        JadbDevice device = getDevice();
        try {
            if (device != null && device.getState() == JadbDevice.State.Device) {
                logger.trace("AdbWrapper: Re-using connection");
                return "Has Device";
            }
        } catch (Exception ex) {
            return ex.getMessage();
        }
        try {
            connection.connectToTcpDevice(address);
            connectionStatus = AdbWrapperStatus.READY;
            return "Found device";
        } catch (IOException e) {
            return e.getMessage();
        } catch (JadbException e) {
            return e.getMessage();
        } catch (ConnectionToRemoteDeviceException e) {
            return e.getMessage();
        }
    }

    @Nullable
    public JadbDevice getDevice() {
        try {
            JadbDevice temp = null;
            if (connectionStatus == AdbWrapperStatus.READY) {
                for (JadbDevice device : connection.getDevices()) {
                    if (device.getSerial().equals(address.toString())) {
                        logger.trace("AdbWrapper: Getting connection: " + device.getSerial() + ", State: " + device.getState());
                        if (device.getState() == JadbDevice.State.Device)
                            return device;
                        temp = device;
                    }
                }
            }
            return temp;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private List<String> batch;

    public void batch(String adbCommand) {
        batch.add(adbCommand);
    }

    public synchronized void exec() {
        if (batch.size() > 0) {
            String cmd = Joiner.on(" && ").join(batch);
            batch.clear();
            exec(cmd);
        }
    }

    private byte[] tempBytes = new byte[1024];

    public synchronized boolean exec(String adbCommand) {
        JadbDevice device = connect();

        if (device == null) return false;

        try {
            InputStream inputStream = device.executeShell(adbCommand);

            final long startTime = System.nanoTime();

            int len;
            while ((len = inputStream.read(tempBytes, 0, tempBytes.length)) > 0) {

            }

            long endTime = System.nanoTime();
            long diff = endTime - startTime;
            logger.trace("AdbCommand: " + adbCommand + " (" + String.format("%2.2f", ((float) diff / 1000000000.0)) + "s)");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JadbException e) {
            e.printStackTrace();
        }
        return false;
    }
}
