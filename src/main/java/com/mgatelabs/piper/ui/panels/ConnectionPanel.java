package com.mgatelabs.piper.ui.panels;

import com.mgatelabs.piper.shared.details.ConnectionDefinition;
import com.mgatelabs.piper.shared.helper.DeviceHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/20/2017.
 */
public class ConnectionPanel extends JInternalFrame {

    private final ConnectionDefinition connectionDefinition;
    private final DeviceHelper deviceHelper;

    private JTextField ipAddress;

    public ConnectionPanel(final ConnectionDefinition connectionDefinition) {
        super("Device Connection (Helper)", false, false, false, false);
        this.connectionDefinition = connectionDefinition;
        deviceHelper = new DeviceHelper(this.connectionDefinition.getIp());
        build();
    }

    public DeviceHelper getDeviceHelper() {
        return deviceHelper;
    }

    private void build() {

        setMinimumSize(new Dimension(300, 100));
        setPreferredSize(getMinimumSize());

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        JLabel ipLabel;

        JPanel container = new JPanel();
        container.setLayout(new GridBagLayout());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 1.0f;
        c.insets = new Insets(5, 5, 5, 5);
        this.add(container, c);

        ipLabel = new JLabel("IP Address:");
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.insets = new Insets(0, 0, 0, 0);
        container.add(ipLabel, c);

        ipAddress = new JTextField();
        ipAddress.setText(connectionDefinition != null ? connectionDefinition.getIp() : "");
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 1.0;
        container.add(ipAddress, c);

        /*
        JButton saveButton = new JButton("Save / Update");
        saveButton.setMnemonic('s');
        saveButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectionDefinition.setIp(ipAddress.getText());
                deviceHelper.setIpAddress(connectionDefinition.getIp());
                connectionDefinition.write();
            }
        });
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = 1.0f;
        c.insets = new Insets(5, 5, 5, 5);
        this.add(saveButton, c);
        */

        pack();

        setVisible(true);
    }
}
