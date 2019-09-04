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

    enum ConnectionType {
        WIFI,
        SERIAL
    }

    enum AdbWrapperStatus {
        INIT,
        READY,
        FAILED
    }

    private JadbDevice targetedDevice;

    private final ConnectionType type;
    private final JadbConnection connection;
    private final InetSocketAddress address;
    private String serial;
    private AdbWrapperStatus connectionStatus;

    public AdbWrapper(final String path, final int port) {
        this(ConnectionType.WIFI, path, port, path + ":" + port);

        serial = path + ":" + port;
    }

    public AdbWrapper(final String serial) {
        this(ConnectionType.SERIAL, "127.0.0.1", 5555, serial);
    }

    private AdbWrapper(ConnectionType type, String host, int port, String serial) {
        this.type = type;
        this.serial = serial;
        this.address = InetSocketAddress.createUnresolved(host, port);
        batch = Lists.newArrayList();
        connectionStatus = AdbWrapperStatus.INIT;
        connection = new JadbConnection();
        targetedDevice = null;
    }

    public void shutdown() {

    }

    public boolean connect() {

        if (targetedDevice != null) {
            try {
                switch (targetedDevice.getState()) {
                    case Device:
                        return true;
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage());
                targetedDevice = null;
            }
        }

        boolean wasSeen = false;

        try {
            for (JadbDevice device : connection.getDevices()) {
                if (device.getSerial().equals(serial)) {
                    wasSeen = true;
                    switch (targetedDevice.getState()) {
                        case Device:
                            targetedDevice = device;
                            return true;
                    }
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            targetedDevice = null;
        }

        if (!wasSeen && type == ConnectionType.WIFI) {
            try {
                connection.connectToTcpDevice(this.address);

                for (JadbDevice device : connection.getDevices()) {
                    if (device.getSerial().equals(serial)) {
                        switch (targetedDevice.getState()) {
                            case Device:
                                targetedDevice = device;
                                return true;
                        }
                    }
                }

                logger.error("Could not obtain connection to Remote ADB");

            } catch (Exception ex) {
                logger.error(ex.getMessage());
                targetedDevice = null;
            }
        }

        return false;
    }

    public JadbDevice getTargetedDevice() {
        return targetedDevice;
    }

    public String status() {

        StringBuilder sb = new StringBuilder();

        List<JadbDevice> devices = Lists.newArrayList();

        try {
            devices.addAll(connection.getDevices());
        } catch (Exception e) {
            sb.append("Could not list devices,");
        }

        for (JadbDevice device : devices) {
            sb.append(device.getSerial());
            try {
                sb.append("[");
                sb.append(device.getState().name());
            } catch (Exception ex) {
                sb.append(ex.getMessage());
            }
            sb.append("],");
        }

        JadbDevice device = getDevice();
        try {
            if (device != null && device.getState() == JadbDevice.State.Device) {
                logger.trace("AdbWrapper: Re-using connection");
                sb.append("Has Device");
            }
        } catch (Exception ex) {
            sb.append(ex.getMessage());
        }
        try {
            connection.connectToTcpDevice(address);
            connectionStatus = AdbWrapperStatus.READY;
            sb.append("Found device");
        } catch (IOException e) {
            sb.append(e.getMessage());
        } catch (JadbException e) {
            sb.append(e.getMessage());
        } catch (ConnectionToRemoteDeviceException e) {
            sb.append(e.getMessage());
        }
        return sb.toString();
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
        JadbDevice device = getTargetedDevice();

        if (device == null) return false;

        try {
            InputStream inputStream = device.executeShell(adbCommand + " && " + AdbShell.ECHO);

            final long startTime = System.nanoTime();

            int len, read = 0;
            while ((len = inputStream.read(tempBytes, 0, tempBytes.length)) > 0) {
                read += len;
            }

            long endTime = System.nanoTime();
            long diff = endTime - startTime;
            logger.trace("AdbCommand: " + adbCommand + " [" + read + "]" + " (" + String.format("%2.2f", ((float) diff / 1000000000.0)) + "s)");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JadbException e) {
            e.printStackTrace();
        }
        return false;
    }
}
