package com.mgatelabs.piper.ui.panels;

import com.mgatelabs.piper.shared.details.PlayerDefinition;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/12/2017.
 */
public class PlayerPanel extends JInternalFrame {

    final PlayerDefinition playerDefinition;

    private JTextField ipAddress;
    private JSlider levelSlider;
    private JLabel levelValueLabel;
    private JLabel energyValueLabel;

    public PlayerPanel(PlayerDefinition playerDefinition) {
        super("Player Info", false, false, false, false);

        this.playerDefinition = playerDefinition;

        setMinimumSize(new Dimension(300, 100));
        setPreferredSize(getMinimumSize());
        //setMaximumSize(getMinimumSize());

        JPanel container = new JPanel();
        container.setLayout(new GridBagLayout());
        this.add(container);

        GridBagConstraints c;
        c = new GridBagConstraints();

        JLabel levelLabel = new JLabel("Level:");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1.0f;
        c.gridwidth = 1;
        container.add(levelLabel, c);

        levelValueLabel = new JLabel(Integer.toString(playerDefinition.getLevel()));
        levelValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1.0f;
        c.gridwidth = 1;
        container.add(levelValueLabel, c);

        JLabel energyLabel = new JLabel("Energy:");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 1;
        c.weightx = 1.0f;
        c.gridwidth = 1;
        container.add(energyLabel, c);

        energyValueLabel = new JLabel(Integer.toString(playerDefinition.getTotalEnergy()));
        energyValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.gridy = 1;
        c.weightx = 1.0f;
        c.gridwidth = 1;
        container.add(energyValueLabel, c);

        levelSlider = new JSlider(15, PlayerDefinition.MAX_LEVEL);
        levelSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                playerDefinition.setLevel(levelSlider.getValue());
                levelValueLabel.setText(Integer.toString(playerDefinition.getLevel()));
                energyValueLabel.setText(Integer.toString(playerDefinition.getTotalEnergy()));
            }
        });
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 4;
        c.weighty = 1.0;
        container.add(levelSlider, c);

        levelSlider.setValue(playerDefinition.getLevel());

        JButton saveButton = new JButton("Save / Update");
        saveButton.setMnemonic('s');
        saveButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playerDefinition.write();
            }
        });
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 4;
        c.weighty = 0;
        container.add(saveButton, c);

        pack();

        setVisible(true);
    }


}
