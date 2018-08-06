package com.mgatelabs.ffbe.shared.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/12/2017.
 */
public class AdbShell {

    Logger logger = Logger.getLogger("AdbShell");

    private ProcessBuilder builder;
    private Process adb;
    private static final byte[] LS = new byte[]{0x0a};
    private static final byte[] ECHO = "uptime".getBytes();

    //private char[] ECHO_KEY = {'9', '8', '7', '6', '1', '2', '3', '4'};
    //ailsa_ii:/

    private String ECHO_KEY = "load average";

    private OutputStream processInput;
    private InputStream processOutput;

    private boolean ready;

    private List<String> batch;

    public static String enableRemote() {
        return commonHandler(new ProcessBuilder("adb", "tcpip", "5555"));
    }

    public static String killServer() {
        return commonHandler(new ProcessBuilder("adb", "kill-server"));
    }

    public static String connect(final String address) {
        return commonHandler(new ProcessBuilder("adb", "connect", address));
    }

    public static String devices() {
        return commonHandler(new ProcessBuilder("adb", "devices"));
    }

    public static String enableUsb() {
        return commonHandler(new ProcessBuilder("adb", "usb"));
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

    public AdbShell() {
        batch = Lists.newArrayList();

        builder = new ProcessBuilder("adb", "shell");
        builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        try {
            adb = builder.start();
            if (!adb.isAlive()) {
                System.out.println("Exit Code: " + adb.exitValue());
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
            String cmd = Joiner.on('\n').join(batch);
            batch.clear();
            exec(cmd);
        }
    }

    public synchronized void exec(String adbCommand) {
        if (!ready) {
            System.out.println("Not Ready");
            return;
        }

        if (!adb.isAlive()) {
            System.out.println("Exit Code: " + adb.exitValue());
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
                    if (buffer[i] == ECHO_KEY.charAt(index)) {
                        //System.out.println("KEY");
                        index++;
                        if (index >= ECHO_KEY.length()) {
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
            logger.finest("AdbCommand: " + adbCommand + " (" + String.format("%2.2f", ((float) diff / 1000000000.0)) + "s)");

        } catch (Exception ex) {
            ex.printStackTrace();
            ready = false;
        }
    }

}
