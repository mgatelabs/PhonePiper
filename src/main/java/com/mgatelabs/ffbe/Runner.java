package com.mgatelabs.ffbe;

import com.mgatelabs.ffbe.server.ServerRunner;
import com.mgatelabs.ffbe.shared.details.PlayerDefinition;
import com.mgatelabs.ffbe.shared.util.AdbShell;
import com.mgatelabs.ffbe.shared.util.AdbUtils;
import com.mgatelabs.ffbe.shared.util.Closer;
import com.mgatelabs.ffbe.ui.FrameChoices;
import com.mgatelabs.ffbe.ui.frame.MainFrame;
import com.mgatelabs.ffbe.ui.frame.StartupFrame;
import com.mgatelabs.ffbe.ui.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.File;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class Runner {

    public static final String VERSION;

    public static File WORKING_DIRECTORY = new File(".");

    static {
        BufferedInputStream bui = new BufferedInputStream(Runner.class.getClassLoader().getResourceAsStream("version.txt"));
        StringBuilder sb = new StringBuilder();
        try {
            int c;
            while ((c = bui.read()) != -1) {
                sb.append((char) c);
            }
        } catch (Exception ex) {
            sb.append("??");
        } finally {
            Closer.close(bui);
        }
        VERSION = sb.toString();
    }

    public static void handleStaticArgs(final String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if (StringUtils.equalsIgnoreCase(args[i], "-working")) {
                WORKING_DIRECTORY = new File(args[i + 1]);
                break;
            }
        }
    }

    public static void main(final String[] args) {

        handleStaticArgs(args);

        System.out.println("Working Directory: " + WORKING_DIRECTORY.getAbsolutePath());

        if (args.length >= 1 && "server".equalsIgnoreCase(args[0])) {
            SpringApplication.run(ServerRunner.class, args);
        } else if (args.length >= 1 && "frame".equalsIgnoreCase(args[0])) {

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
                        if (i + 1 < args.length && Constants.ID_PATTERN.matcher(args[i + 1]).matches()) {
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

                    FrameChoices frameChoices = new FrameChoices(startupFrame.getSelectedAction(), startupFrame.getSelectedMode(), playerDefinition, startupFrame.getSelectedMap(), startupFrame.getSelectedScript(), startupFrame.getSelectedScript2(), null, startupFrame.getSelectedDevice(), startupFrame.getSelectedView(), startupFrame.getSelectedView2());

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
