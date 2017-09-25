package com.mgatelabs.ffbe.ui.panels;

import com.mgatelabs.ffbe.shared.details.PlayerDefinition;

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
        super("Device/Player", false, false, false, false);

        this.playerDefinition = playerDefinition;

        setMinimumSize(new Dimension(300, 135));
        setPreferredSize(getMinimumSize());
        //setMaximumSize(getMinimumSize());

        JPanel container = new JPanel();
        container.setLayout(new GridBagLayout());
        this.add(container);

        GridBagConstraints c;
        c = new GridBagConstraints();

        /*
        GridBagConstraints c;
        JLabel ipLabel = new JLabel("IP Address:");
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        container.add(ipLabel, c);

        ipAddress = new JTextField();
        ipAddress.setText(playerDefinition.getIp());
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 4;
        container.add(ipAddress, c);
        */

        JLabel levelLabel = new JLabel("Level:");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1.0f;
        c.gridwidth = 1;
        container.add(levelLabel, c);

        levelValueLabel = new JLabel(Integer.toString(playerDefinition.getLevel()));
        levelValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 1.0f;
        c.gridwidth = 1;
        container.add(levelValueLabel, c);

        JLabel energyLabel = new JLabel("Energy:");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 2;
        c.weightx = 1.0f;
        c.gridwidth = 1;
        container.add(energyLabel, c);

        energyValueLabel = new JLabel(Integer.toString(playerDefinition.getTotalEnergy()));
        energyValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.gridy = 2;
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
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 4;
        container.add(levelSlider, c);

        levelSlider.setValue(playerDefinition.getLevel());

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //playerDefinition.setIp(ipAddress.getText());
                playerDefinition.write();
            }
        });
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 4;
        c.weighty = 1.0f;
        container.add(saveButton, c);

        pack();

        setVisible(true);
    }


}
