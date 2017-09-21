package com.mgatelabs.ffbe.ui.panels;

import com.mgatelabs.ffbe.shared.details.ScriptDefinition;
import com.mgatelabs.ffbe.shared.details.ViewDefinition;
import com.mgatelabs.ffbe.shared.helper.DeviceHelper;
import com.mgatelabs.ffbe.shared.util.AdbShell;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/20/2017.
 */
public class RunScriptPanel extends JInternalFrame {

    private DeviceHelper helper;
    private AdbShell shell;
    private ViewDefinition viewDefinition;
    private ScriptDefinition scriptDefinition;
    private MapPanel mapPanel;

    private Timer timer;

    private JButton startStopButton;


    public RunScriptPanel(DeviceHelper helper, AdbShell shell, ViewDefinition viewDefinition, ScriptDefinition scriptDefinition, MapPanel mapPanel) {
        super("Script Runner", true, false, true, false);
        this.helper = helper;
        this.shell = shell;
        this.viewDefinition = viewDefinition;
        this.scriptDefinition = scriptDefinition;
        this.mapPanel = mapPanel;

        build();
    }

    private void build() {
        setMinimumSize(new Dimension(300,100));
        setPreferredSize(getMinimumSize());

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        startStopButton = new JButton("Start / Stop");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 1.0f;
        c.insets = new Insets(5, 5, 5, 5);
        this.add(startStopButton, c);

        pack();

        setVisible(true);

        timer = new Timer(500, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }
}
