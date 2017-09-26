package com.mgatelabs.ffbe;

import com.mgatelabs.ffbe.runners.GameManager;
import com.mgatelabs.ffbe.shared.details.*;
import com.mgatelabs.ffbe.shared.util.AdbShell;
import com.mgatelabs.ffbe.shared.util.AdbUtils;
import com.mgatelabs.ffbe.shared.util.ConsoleInput;
import com.mgatelabs.ffbe.ui.FrameChoices;
import com.mgatelabs.ffbe.ui.frame.MainFrame;
import com.mgatelabs.ffbe.ui.frame.StartupFrame;

import javax.swing.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class Runner {
    public static void main(String[] args) {


        boolean showHelp = false;

        if (args.length >= 1) {
            if ("manage".equalsIgnoreCase(args[0]) || "manager".equalsIgnoreCase(args[0])) {
                DeviceDefinition deviceDefinition = null;
                if (args.length == 2) {
                    deviceDefinition = DeviceDefinition.read(args[1]);
                } else {
                    deviceDefinition = DeviceDefinition.read("axon7");
                }
                if (deviceDefinition == null) {
                    System.out.println("Could not find a device definition");
                }

                GameManager manager = new GameManager(deviceDefinition);

                manager.manage();
            } else if ("gui".equalsIgnoreCase(args[0])) {

                PlayerDefinition playerDefinition = PlayerDefinition.read();

                final ImageIcon imageIcon = new ImageIcon(playerDefinition.getClass().getResource("/icon.png"));

                while (true) {
                    StartupFrame startupFrame = new StartupFrame(playerDefinition, imageIcon);

                    while (startupFrame.isShowing()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (startupFrame.getSelectedAction() != null && startupFrame.getSelectedMode() != null) {

                        FrameChoices frameChoices = new FrameChoices(startupFrame.getSelectedAction(), startupFrame.getSelectedMode(), playerDefinition, startupFrame.getSelectedMap(), startupFrame.getSelectedScript(), startupFrame.getSelectedDevice(), startupFrame.getSelectedView());

                        if (frameChoices.isValid()) {
                            MainFrame frame = new MainFrame(frameChoices, imageIcon);

                            while (frame.isShowing()) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (!frame.isReturnRequested()) {
                                return;
                            }
                        } else {

                        }
                    } else {
                        return;
                    }
                }

                //new MainFrame().setVisible(true);
            } else if ("frame".equalsIgnoreCase(args[0])) {

                long startTime = System.nanoTime();
                AdbUtils.persistScreen(new AdbShell());
                long endTime = System.nanoTime();

                long dif = endTime - startTime;

                System.out.println("Frame captured in: " + ((float) dif / 1000000000.0) + "s");

        /*
        File sampleFile = new File("pieces//friendrequest.png");

        BufferedImage bufferedImage = null;

        try {
          bufferedImage = ImageIO.read(sampleFile);
        } catch (IOException e) {
          e.printStackTrace();
          return;
        }

        ImageWrapper rawImageWrapper = new PngImageWrapper(bufferedImage);// GameRunner.getScreen();

        ImagePixelPickerDialog dialog = new ImagePixelPickerDialog(ImagePixelPickerDialog.Mode.BOX);
        dialog.setup(rawImageWrapper, new ArrayList<>());
        dialog.start();
      */

                //runner.snap();
                return;
            } /*else if ("mapper".equalsIgnoreCase(args[0])) {
        final String phoneName = args.length == 1 ? "axon7" : args[2];
        Phone phone = runner.loadPhone(phoneName);
        DungeonMapperCommandLine dungeonMapperCommandLine = new DungeonMapperCommandLine(phone);
        dungeonMapperCommandLine.run();
        return;
      }*/ else if ("script".equalsIgnoreCase(args[0])) {

                if (args.length < 2) {
                    showHelp = true;
                } else {
                    final String scriptName = args[1];
                    final String deviceName = args.length == 2 ? "axon7" : args[2];

                    ScriptDefinition script = ScriptDefinition.read(scriptName);
                    if (script == null) {
                        System.out.println("Could not locate script: " + scriptName);
                        return;
                    }
                    DeviceDefinition device = DeviceDefinition.read(deviceName);
                    if (device == null) {
                        System.out.println("Could not locate device: " + deviceName);
                        return;
                    }
                    ViewDefinition view = ViewDefinition.read(device.getViewId());
                    if (view == null) {
                        System.out.println("Could not locate view: " + device.getViewId());
                        return;
                    }
                    ConnectionDefinition connectionDefinition = ConnectionDefinition.read();
                    PlayerDefinition playerDefinition = PlayerDefinition.read();
                    if (playerDefinition == null) {
                        playerDefinition = new PlayerDefinition();
                        while (true) {
                            System.out.println("Please enter your current Player Level: ");
                            int level = ConsoleInput.getInt();
                            if (level > 8 && level <= 150) {
                                playerDefinition.setLevel(level);
                                playerDefinition.write();
                                break;
                            } else {
                                System.out.println("Invalid level, it must be between 8 and 150.  Stopping.");
                                return;
                            }
                        }

                    }
                    //ScriptRunner scriptRunner = new ScriptRunner(playerDetail, connectionDefinition, script, device, view);

                    System.out.println("----------");
                    System.out.println("Script: " + scriptName);
                    System.out.println("Device: " + deviceName);
                    System.out.println("View: " + device.getViewId());
                    System.out.println("Energy: " + playerDefinition.getTotalEnergy());
                    System.out.println("----------");
                    System.out.println();

                    //scriptRunner.run("main");
                }


            }
        } else {
            showHelp = true;
        }

        if (showHelp) {
            System.out.println("How to use:");
            System.out.println();
            System.out.println("script (scriptName) [deviceName]");
            System.out.println("\tRun a script for a given device.  Will default to Axon7 device.");
            System.out.println("manage [deviceName]");
            System.out.println("\tEdit a device's screens and components.  Will default to Axon7 device.");
            System.out.println("frame");
            System.out.println("\tCapture the current frame to your device for later inspection");
        }
    }
}
