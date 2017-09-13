package com.mgatelabs.ffbe.ui;

import com.mgatelabs.ffbe.shared.details.PlayerDetail;

import javax.swing.*;
import java.awt.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/12/2017.
 */
public class PlayerPanel extends JInternalFrame {

    private JTextField ipAddress;
    private JSlider levelSlider;

    public PlayerPanel() {
        super("Player Info", false, false, false, false);

        setMinimumSize(new Dimension(256, 150));
        setPreferredSize(getMinimumSize());
        setMaximumSize(getMinimumSize());

        JPanel container = new JPanel();
        container.setLayout(new GridBagLayout());
        this.add(container);

        GridBagConstraints c;
        JLabel ipLabel = new JLabel("IP Address:");
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        container.add(ipLabel, c);

        ipAddress = new JTextField();
        c.gridx = 0;
        c.gridy = 1;
        container.add(ipAddress, c);

        JLabel levelLabel = new JLabel("Player Level:");
        c.gridx = 0;
        c.gridy = 2;
        container.add(levelLabel, c);

        levelSlider = new JSlider(15, PlayerDetail.MAX_LEVEL);
        c.gridx = 0;
        c.gridy = 3;
        container.add(levelSlider, c);

        JButton saveButton = new JButton("Save");
        c.gridx = 0;
        c.gridy = 4;
        container.add(saveButton, c);

        pack();

        setVisible(true);
    }


}
