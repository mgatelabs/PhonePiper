package com.mgatelabs.ffbe.ui.panels;

import com.mgatelabs.ffbe.shared.details.ScriptDefinition;
import com.mgatelabs.ffbe.shared.details.StateDefinition;
import com.mgatelabs.ffbe.shared.details.ViewDefinition;
import com.mgatelabs.ffbe.shared.helper.DeviceHelper;
import com.mgatelabs.ffbe.shared.util.AdbShell;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

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

    private JComboBox<StateDefinition> stateCombo;

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
        setMinimumSize(new Dimension(300,150));
        setPreferredSize(getMinimumSize());

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        startStopButton = new JButton("Start / Stop");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 1.0f;
        c.insets = new Insets(5, 5, 5, 5);
        this.add(startStopButton, c);

        JLabel tempLabel = new JLabel("State:");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        c.gridwidth = 1;
        add(tempLabel, c);

        stateCombo = new JComboBox<>();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = 1.0f;
        //c.insets = new Insets(5, 5, 5, 5);
        this.add(stateCombo, c);

        SortedSet<StateDefinition> definitions = new TreeSet<>(new Comparator<StateDefinition>() {
            @Override
            public int compare(StateDefinition o1, StateDefinition o2) {
                if (o1.getId().equals("main")) {
                    return -1;
                } else if (o2.getId().equals("main")) {
                    return 1;
                }
                return o1.getId().compareTo(o2.getId());
            }
        });
        definitions.addAll(scriptDefinition.getStates().values());

        for (StateDefinition stateDefinition: definitions) {
            stateCombo.addItem(stateDefinition);
        }

        pack();

        setVisible(true);

        timer = new Timer(500, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }
}
