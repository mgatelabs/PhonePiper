package com.mgatelabs.ffbe.shared.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/12/2017.
 */
public class AdbShell {

    Logger logger = Logger.getLogger("AdbShell");

    private ProcessBuilder builder;
    private Process adb;
    private static final byte[] LS = "\n".getBytes();
    private static final byte[] ECHO = "doesnotexist".getBytes();

    //private char[] ECHO_KEY = {'9', '8', '7', '6', '1', '2', '3', '4'};
    //ailsa_ii:/

    private String ECHO_KEY = "doesnotexist: not found";

    private OutputStream processInput;
    private InputStream processOutput;

    private boolean ready;

    private List<String> batch;

    public AdbShell() {
        batch = Lists.newArrayList();

        builder = new ProcessBuilder("adb", "shell");
        try {
            adb = builder.start();
            ready = true;

            // reads from the process output
            processInput = adb.getOutputStream();

            // sends to process's input
            processOutput = adb.getInputStream();

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
        final long startTime = System.nanoTime();
        try {
            //System.out.println("\n\nWorking On: " + adbCommand);

            processInput.write(adbCommand.getBytes());
            processInput.write(LS);
            processInput.write(ECHO);
            processInput.write(LS);
            processInput.flush();

            // The idea is to run a command and a failing command.  The failure will result in a message, which can easily be picked up when executed.

            int c;
            byte[] buffer = new byte[32];
            int index = 0;
            boolean exitFound = false;
            while (!exitFound && (c = processOutput.read(buffer)) != -1) {
                for (int i = 0; i < buffer.length; i++) {
                    if (buffer[i] == ECHO_KEY.charAt(index)) {
                        index++;
                        if (index >= ECHO_KEY.length()) {
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
