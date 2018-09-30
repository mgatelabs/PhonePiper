package com.mgatelabs.piper.ui.panels;

import com.mgatelabs.piper.shared.util.AdbShell;

import javax.swing.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/21/2017 for Phone-Piper
 */
public class AdbPanel extends JInternalFrame {

    private final AdbShell adbShell;

    public AdbPanel(AdbShell adbShell) {
        super("ADB Status", true, false, false, false);
        this.adbShell = adbShell;
    }

    private void build() {


        pack();

        setVisible(true);
    }
}
