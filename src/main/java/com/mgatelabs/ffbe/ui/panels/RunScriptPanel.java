package com.mgatelabs.ffbe.ui.panels;

import com.google.common.collect.Maps;
import com.mgatelabs.ffbe.runners.ScriptRunner;
import com.mgatelabs.ffbe.shared.details.*;
import com.mgatelabs.ffbe.shared.helper.DeviceHelper;
import com.mgatelabs.ffbe.shared.util.AdbShell;
import com.mgatelabs.ffbe.ui.utils.CustomHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Map;
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

    private ScriptRunner scriptRunner;

    private ScriptThread scriptThread;

    private Map<String, Integer> stateToIntegerMap;

    private JLabel lastImageTimeLabel;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

    public RunScriptPanel(DeviceHelper helper, PlayerDefinition playerDefinition, AdbShell shell, DeviceDefinition deviceDefinition, ViewDefinition viewDefinition, ScriptDefinition scriptDefinition, MapPanel mapPanel, CustomHandler customHandler) {
        super("Script Runner", true, false, true, false);
        this.helper = helper;
        this.shell = shell;
        this.viewDefinition = viewDefinition;
        this.scriptDefinition = scriptDefinition;
        this.mapPanel = mapPanel;

        timer = null;

        scriptRunner = new ScriptRunner(playerDefinition, helper, scriptDefinition, deviceDefinition, viewDefinition, customHandler);

        scriptThread = null;

        stateToIntegerMap = Maps.newHashMap();

        build();
    }

    private void build() {
        setMinimumSize(new Dimension(300,125));
        setPreferredSize(getMinimumSize());

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        startStopButton = new JButton("Start Script");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 1.0f;
        c.insets = new Insets(3, 5, 3, 5);
        startStopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (scriptRunner.getStatus() == ScriptRunner.Status.RUNNING) {
                    scriptRunner.setStatus(ScriptRunner.Status.PAUSED);
                    scriptThread = null;
                    startStopButton.setText("Start Script");
                } else if (scriptRunner.getStatus() != ScriptRunner.Status.PAUSED) {
                    scriptRunner.initHelper();
                    scriptThread = new ScriptThread(scriptRunner, ((StateDefinition)stateCombo.getSelectedItem()).getId());
                    scriptThread.start();
                    timer.start();
                    startStopButton.setText("Pause  Script");
                } else if (scriptRunner.getStatus() == ScriptRunner.Status.PAUSED) {
                    scriptThread = new ScriptThread(scriptRunner, ((StateDefinition)stateCombo.getSelectedItem()).getId());
                    scriptThread.start();
                    timer.start();
                    startStopButton.setText("Pause Script");
                }
            }
        });
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

        definitions.addAll(scriptDefinition.getFilteredStates().values());

        int i = 0;
        for (StateDefinition stateDefinition: definitions) {
            stateCombo.addItem(stateDefinition);
            stateToIntegerMap.put(stateDefinition.getId(), i++);
        }

        tempLabel = new JLabel("Image:");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        c.gridwidth = 1;
        add(tempLabel, c);

        lastImageTimeLabel = new JLabel("?");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        c.weightx = 1.0f;
        //c.insets = new Insets(5, 5, 5, 5);
        this.add(lastImageTimeLabel, c);

        pack();

        setVisible(true);

        timer = new Timer(500, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer neededIndex = stateToIntegerMap.get(scriptRunner.getCurrentStateId());
                if (neededIndex != null) {
                    if (stateCombo.getSelectedIndex() != neededIndex) {
                        stateCombo.setSelectedIndex(neededIndex);
                    }
                }
                if (scriptRunner.getLastImageDate() != null) {
                    lastImageTimeLabel.setText(simpleDateFormat.format(scriptRunner.getLastImageDate()) + String.format(" (%2.2f)", scriptRunner.getLastImageDuration()) );
                }
                if (scriptRunner.getStatus() != ScriptRunner.Status.RUNNING) {
                    timer.stop();
                    startStopButton.setText("Start Script");
                    scriptThread = null;
                }
            }
        });
    }

    public void stop() {
        if (scriptRunner != null) {
            scriptRunner.setStatus(ScriptRunner.Status.PAUSED);
        }
        scriptThread = null;
        startStopButton.setText("Start Script");
    }

    private static class ScriptThread extends Thread {
        ScriptRunner runner;
        String state;
        public ScriptThread(ScriptRunner scriptRunner, String state) {
            this.runner = scriptRunner;
            this.state = state;
        }

        @Override
        public void run() {
            super.run();
            this.runner.run(state);
        }
    }
}
