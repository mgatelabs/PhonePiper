package com.mgatelabs.ffbe.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/12/2017.
 */
public class MainFrame extends JFrame {

    JDesktopPane desktopPane;
    PlayerPanel playerPanel;

    public MainFrame() throws HeadlessException {
        super("FFBExecute 0.0.1");
        setMinimumSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(1024, 768));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        desktopPane = new JDesktopPane();

        playerPanel = new PlayerPanel();

        desktopPane.add(playerPanel);

        this.pack();

        setContentPane(desktopPane);
    }
}
