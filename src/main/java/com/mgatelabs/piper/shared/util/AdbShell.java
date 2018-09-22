package com.mgatelabs.piper.shared.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.mgatelabs.piper.shared.details.DeviceDefinition;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/12/2017.
 */
public class AdbShell {

    Logger logger = Logger.getLogger("AdbShell");

    public static String ADB_PATH = "adb";

    public static String ADB_DIRECT = "";

    private ProcessBuilder builder;
    private Process adb;
    private static final byte[] LS = new byte[]{0x0a};
    private static final byte[] ECHO = "uptime".getBytes();

    //private char[] ECHO_KEY = {'9', '8', '7', '6', '1', '2', '3', '4'};
    //ailsa_ii:/

    private String endLineKey;
    //sleep time

    private OutputStream processInput;
    private InputStream processOutput;

    private boolean ready;

    private List<String> batch;

    public static String enableRemote() {
        return commonHandler(new ProcessBuilder(ADB_PATH, "tcpip", "5555"));
    }

    public static String killServer() {
        return commonHandler(new ProcessBuilder(ADB_PATH, "kill-server"));
    }

    public static String connect(final String address) {
        return commonHandler(new ProcessBuilder(ADB_PATH, "connect", address));
    }

    public static String devices() {
        return commonHandler(new ProcessBuilder(ADB_PATH, "devices"));
    }

    public static String enableUsb() {
        return commonHandler(new ProcessBuilder(ADB_PATH, "usb"));
    }

    public static String getStreamAsString(InputStream inputStream) {
        try {
            if (inputStream.available() > 0) {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                StringBuilder stringBuilder = new StringBuilder();
                while (bufferedInputStream.available() > 0) {
                    stringBuilder.append((char) bufferedInputStream.read());
                }
                return StringUtils.trim(stringBuilder.toString());
            }
        } catch (IOException i) {

        }
        return "";
    }

    public static String commonHandler(ProcessBuilder processBuilder) {
        processBuilder.inheritIO();
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectError(ProcessBuilder.Redirect.PIPE);
        try {
            Process temp = processBuilder.start();
            temp.waitFor();
            String normalString = getStreamAsString(temp.getInputStream());
            String errorString = getStreamAsString(temp.getErrorStream());


            return  normalString.replaceAll("\n", "|").replaceAll("\r", "") + errorString.replaceAll("\n", "|").replaceAll("\r", "");
        } catch (IOException e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        }
    }

    public AdbShell(DeviceDefinition device) {
        batch = Lists.newArrayList();

        endLineKey = device.getAdbEndLine();

        logger.finest("Starting ADb Shell");

        if (StringUtils.isNotBlank(ADB_DIRECT)) {
            builder = new ProcessBuilder(ADB_PATH, "-s", ADB_DIRECT, "shell");
        } else {
            builder = new ProcessBuilder(ADB_PATH, "shell");
        }
        builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        try {
            adb = builder.start();
            if (!adb.isAlive()) {
                logger.severe("Adb Exit Code: " + adb.exitValue());
                ready = false;
                return;
            }
            ready = true;

            // reads from the process output
            processInput = new BufferedOutputStream(adb.getOutputStream());

            // sends to process's input
            processOutput = new BufferedInputStream(adb.getInputStream());

        } catch (IOException e) {
            e.printStackTrace();
            ready = false;
        }
    }

    public void attachhandler(final Handler handler) {
        logger.removeHandler(handler);
        logger.addHandler(handler);
    }

    public void setLevel(Level newLevel) {
        logger.setLevel(newLevel);
    }

    public void shutdown() {
        if (adb.isAlive()) {
            adb.destroyForcibly();
        }
    }

    public boolean isReady() {
        return ready;
    }

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

    public synchronized void exec(String adbCommand) {
        if (!ready) {
            logger.severe("Adb Not Ready");
            return;
        }

        if (!adb.isAlive()) {
            logger.severe("Adb Exit Code: " + adb.exitValue());
            ready = false;
            return;
        }

        final long startTime = System.nanoTime();
        try {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            byteArrayOutputStream.write(adbCommand.getBytes());
            byteArrayOutputStream.write(LS);
            byteArrayOutputStream.write(ECHO);
            byteArrayOutputStream.write(LS);

            processInput.write(byteArrayOutputStream.toByteArray());
            processInput.flush();

            // The idea is to run a command and a failing command.  The failure will result in a message, which can easily be picked up when executed.

            int c;
            byte[] buffer = new byte[256];
            int index = 0;
            boolean exitFound = false;
            while (!exitFound && (c = processOutput.read(buffer)) != -1) {
                //System.out.println("Reading: " + c);
                for (int i = 0; i < c; i++) {
                    if (buffer[i] == endLineKey.charAt(index)) {
                        //System.out.println("KEY");
                        index++;
                        if (index >= endLineKey.length()) {
                            //System.out.println("END KEY");
                            exitFound = true;
                            break;
                        }
                    } else {
                        index = 0;
                    }
                }
            }

            long endTime = System.nanoTime();
            long diff = endTime - startTime;
            if (logger.isLoggable(Level.SEVERE)) {
                logger.finest("AdbCommand: " + adbCommand + " (" + String.format("%2.2f", ((float) diff / 1000000000.0)) + "s)");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            ready = false;
        }
    }

}
