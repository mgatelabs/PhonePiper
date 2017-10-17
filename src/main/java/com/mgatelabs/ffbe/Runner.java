package com.mgatelabs.ffbe;

import com.mgatelabs.ffbe.shared.details.PlayerDefinition;
import com.mgatelabs.ffbe.shared.util.AdbShell;
import com.mgatelabs.ffbe.shared.util.AdbUtils;
import com.mgatelabs.ffbe.ui.FrameChoices;
import com.mgatelabs.ffbe.ui.frame.MainFrame;
import com.mgatelabs.ffbe.ui.frame.StartupFrame;
import com.mgatelabs.ffbe.ui.utils.Constants;

import javax.swing.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class Runner {
    public static void main(String[] args) {

        boolean showHelp = false;

        if ((args.length >= 1 && "gui".equalsIgnoreCase(args[0])) || args.length == 0) {

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

                    if (frameChoices.getAction() == FrameChoices.Action.CREATE) {
                        String inputValue = JOptionPane.showInputDialog("Please input a " + frameChoices.getMode().name());
                        if (inputValue == null || inputValue.trim().length() == 0) {
                            continue;
                        } else if (!Constants.ID_PATTERN.matcher(inputValue).matches()) {
                            JOptionPane.showMessageDialog(null, "Invalid string format, only allow a-z A-Z 0-9 - _");
                            continue;
                        } else if (!frameChoices.canCreate(inputValue)) {
                            JOptionPane.showMessageDialog(null, "Invalid name.  File may already exist.");
                            continue;
                        }
                    }

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
                            break;
                        }
                    } else {

                    }
                } else {
                    break;
                }
            }

            System.exit(0);
            //new MainFrame().setVisible(true);
        } else if (args.length >= 1) {
            if ("frame".equalsIgnoreCase(args[0])) {

                long startTime = System.nanoTime();
                AdbUtils.persistScreen(new AdbShell());
                long endTime = System.nanoTime();

                long dif = endTime - startTime;

                System.out.println("Frame captured in: " + ((float) dif / 1000000000.0) + "s");

                return;
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
