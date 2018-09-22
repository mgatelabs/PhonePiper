package com.mgatelabs.piper;

import com.google.common.collect.Lists;
import com.mgatelabs.piper.server.ServerRunner;
import com.mgatelabs.piper.shared.details.PlayerDefinition;
import com.mgatelabs.piper.shared.util.Closer;
import com.mgatelabs.piper.shared.util.Loggers;
import com.mgatelabs.piper.ui.FrameChoices;
import com.mgatelabs.piper.ui.frame.MainFrame;
import com.mgatelabs.piper.ui.frame.StartupFrame;
import com.mgatelabs.piper.ui.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.util.List;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class Runner {

    public static final String VERSION;

    public static File WORKING_DIRECTORY = new File(".");
    public static String ADB_NAME = "adb";

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
            if (StringUtils.equalsIgnoreCase(args[i], "-adb")) {
                ADB_NAME = args[i + 1];
                break;
            }
        }
    }

    public static void main(final String[] args) {
        handleStaticArgs(args);

        Loggers.init();

        System.out.println("Working Directory: " + WORKING_DIRECTORY.getAbsolutePath());

        if (args.length == 0 || (args.length >= 1 && "server".equalsIgnoreCase(args[0]))) {
            // Just run the server and let it handle everything else
            SpringApplication.run(ServerRunner.class, args);
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

                    List<String> views = Lists.newArrayList();
                    if (StringUtils.isNotBlank(startupFrame.getSelectedView())) views.add(startupFrame.getSelectedView());
                    if (StringUtils.isNotBlank(startupFrame.getSelectedView2())) views.add(startupFrame.getSelectedView2());

                    List<String> scripts = Lists.newArrayList();
                    if (StringUtils.isNotBlank(startupFrame.getSelectedScript())) scripts.add(startupFrame.getSelectedScript());
                    if (StringUtils.isNotBlank(startupFrame.getSelectedScript2())) scripts.add(startupFrame.getSelectedScript2());

                    FrameChoices frameChoices = new FrameChoices(startupFrame.getSelectedAction(), startupFrame.getSelectedMode(), playerDefinition, startupFrame.getSelectedMap(), startupFrame.getSelectedDevice(), views, scripts);

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
