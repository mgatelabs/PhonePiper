package com.mgatelabs.ffbe.ui;

import com.mgatelabs.ffbe.shared.details.PlayerDetail;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/12/2017.
 */
public class PlayerPanel extends JInternalFrame {

    final PlayerDetail playerDetail;

    private JTextField ipAddress;
    private JSlider levelSlider;
    private JLabel levelValueLabel;
    private JLabel energyValueLabel;

    public PlayerPanel(PlayerDetail playerDetail) {
        super("Device/Player", false, false, false, false);

        this.playerDetail = playerDetail;

        setMinimumSize(new Dimension(300, 135));
        setPreferredSize(getMinimumSize());
        //setMaximumSize(getMinimumSize());

        JPanel container = new JPanel();
        container.setLayout(new GridBagLayout());
        this.add(container);

        GridBagConstraints c;
        JLabel ipLabel = new JLabel("IP Address:");
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        container.add(ipLabel, c);

        ipAddress = new JTextField();
        ipAddress.setText(playerDetail.getIp());
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 4;
        container.add(ipAddress, c);

        JLabel levelLabel = new JLabel("Level:");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1.0f;
        c.gridwidth = 1;
        container.add(levelLabel, c);

        levelValueLabel = new JLabel(Integer.toString(playerDetail.getLevel()));
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

        energyValueLabel = new JLabel(Integer.toString(playerDetail.getTotalEnergy()));
        energyValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.gridy = 2;
        c.weightx = 1.0f;
        c.gridwidth = 1;
        container.add(energyValueLabel, c);

        levelSlider = new JSlider(15, PlayerDetail.MAX_LEVEL);
        levelSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                playerDetail.setLevel(levelSlider.getValue());
                levelValueLabel.setText(Integer.toString(playerDetail.getLevel()));
                energyValueLabel.setText(Integer.toString(playerDetail.getTotalEnergy()));
            }
        });
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 4;
        container.add(levelSlider, c);

        levelSlider.setValue(playerDetail.getLevel());

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playerDetail.setIp(ipAddress.getText());
                playerDetail.write();
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
