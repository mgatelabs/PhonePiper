package com.mgatelabs.ffbe;

import com.mgatelabs.ffbe.shared.details.PlayerDefinition;
import com.mgatelabs.ffbe.shared.util.AdbShell;
import com.mgatelabs.ffbe.shared.util.AdbUtils;
import com.mgatelabs.ffbe.ui.FrameChoices;
import com.mgatelabs.ffbe.ui.frame.MainFrame;
import com.mgatelabs.ffbe.ui.frame.StartupFrame;
import com.mgatelabs.ffbe.ui.utils.Constants;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class Runner {
    public static void main(final String[] args) {

        if (args.length >= 1 && "frame".equalsIgnoreCase(args[0])) {

            long startTime = System.nanoTime();
            AdbUtils.persistScreen(new AdbShell());
            long endTime = System.nanoTime();

            long dif = endTime - startTime;

            System.out.println("Frame captured in: " + ((float) dif / 1000000000.0) + "s");

            return;
        } else {

            PlayerDefinition playerDefinition = PlayerDefinition.read();

            final ImageIcon imageIcon = new ImageIcon(playerDefinition.getClass().getResource("/icon.png"));

            while (true) {

                String postfix = null;
                for (int i = 0; i < args.length - 1; i++) {
                    if (StringUtils.equalsIgnoreCase(args[i], "-postfix")) {
                        if (i + 1 < args.length && Constants.ID_PATTERN.matcher(args[i+1]).matches()) {
                            postfix = args[i + 1];
                            break;
                        }
                    }
                }

                StartupFrame startupFrame = new StartupFrame(playerDefinition, imageIcon, postfix);

                while (startupFrame.isShowing()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (startupFrame.getSelectedAction() != null && startupFrame.getSelectedMode() != null) {

                    FrameChoices frameChoices = new FrameChoices(startupFrame.getSelectedAction(), startupFrame.getSelectedMode(), playerDefinition, startupFrame.getSelectedMap(), startupFrame.getSelectedScript(), startupFrame.getSelectedScript2(), startupFrame.getSelectedDevice(), startupFrame.getSelectedView(), startupFrame.getSelectedView2());

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
        }
    }
}
