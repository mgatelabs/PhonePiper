package com.mgatelabs.piper.ui.panels;

import com.google.common.collect.Maps;
import com.mgatelabs.piper.runners.ScriptRunner;
import com.mgatelabs.piper.shared.details.*;
import com.mgatelabs.piper.shared.helper.DeviceHelper;
import com.mgatelabs.piper.shared.util.AdbShell;
import com.mgatelabs.piper.ui.utils.WebLogHandler;

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
public class RunScriptPanel extends JToolBar {

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

    private final Icon playIcon;
    private final Icon pauseIcon;

    public RunScriptPanel(DeviceHelper helper, ConnectionDefinition connectionDefinition, PlayerDefinition playerDefinition, AdbShell shell, DeviceDefinition deviceDefinition, ViewDefinition viewDefinition, ScriptDefinition scriptDefinition, MapPanel mapPanel, WebLogHandler webLogHandler) {
        super("Run", JToolBar.HORIZONTAL);

        this.helper = helper;
        this.shell = shell;
        this.viewDefinition = viewDefinition;
        this.scriptDefinition = scriptDefinition;
        this.mapPanel = mapPanel;

        timer = null;

        scriptRunner = new ScriptRunner(playerDefinition, connectionDefinition, helper, scriptDefinition, deviceDefinition, viewDefinition, webLogHandler, null);

        scriptThread = null;

        stateToIntegerMap = Maps.newHashMap();

        playIcon = new ImageIcon( this.getClass().getClassLoader().getResource("play.png"));
        pauseIcon = new ImageIcon(this.getClass().getClassLoader().getResource("pause.png"));

        build();
    }

    private void build() {
        setMinimumSize(new Dimension(400,64));
        setPreferredSize(getMinimumSize());

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        startStopButton = new JButton();
        startStopButton.setPreferredSize(new Dimension(64,64));
        startStopButton.setMinimumSize(new Dimension(64,64));
        startStopButton.setMaximumSize(new Dimension(64,64));
        c.fill = GridBagConstraints.VERTICAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 2;
        c.weightx = 0;
        c.weighty = 1.0;
        c.insets = new Insets(2, 2, 2, 5);
        startStopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (scriptRunner.getStatus() == ScriptRunner.Status.RUNNING) {
                    scriptRunner.setStatus(ScriptRunner.Status.PAUSED);
                    scriptThread = null;
                    changeIcon(true);
                } else if (scriptRunner.getStatus() != ScriptRunner.Status.PAUSED) {
                    scriptThread = new ScriptThread(scriptRunner, ((StateDefinition)stateCombo.getSelectedItem()).getId());
                    scriptThread.start();
                    timer.start();
                    changeIcon(false);
                } else if (scriptRunner.getStatus() == ScriptRunner.Status.PAUSED) {
                    scriptThread = new ScriptThread(scriptRunner, ((StateDefinition)stateCombo.getSelectedItem()).getId());
                    scriptThread.start();
                    timer.start();
                    changeIcon(false);
                }
            }
        });
        this.add(startStopButton, c);

        changeIcon(true);

        JLabel tempLabel = new JLabel("Current State:");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 0;
        c.gridheight = 1;
        c.weightx = 0;
        c.gridwidth = 1;
        add(tempLabel, c);

        stateCombo = new JComboBox<>();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
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

        tempLabel = new JLabel("Image Timing:");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.gridy = 0;
        c.weightx = 0;
        c.gridwidth = 1;
        add(tempLabel, c);

        lastImageTimeLabel = new JLabel("?");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = 1.0f;
        //c.insets = new Insets(5, 5, 5, 5);
        this.add(lastImageTimeLabel, c);

        //setVisible(true);

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
                    changeIcon(true);
                    scriptThread = null;
                }
            }
        });
    }

    public void changeIcon(boolean isPlay) {
        startStopButton.setIcon(isPlay ? playIcon : pauseIcon);
    }

    public void stop() {
        if (scriptRunner != null) {
            scriptRunner.setStatus(ScriptRunner.Status.PAUSED);
        }
        scriptThread = null;
        changeIcon(true);
    }

    public static class ScriptThread extends Thread {
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
