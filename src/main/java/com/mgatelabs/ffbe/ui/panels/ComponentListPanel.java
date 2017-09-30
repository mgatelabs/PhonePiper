package com.mgatelabs.ffbe.ui.panels;

import com.mgatelabs.ffbe.shared.details.ActionType;
import com.mgatelabs.ffbe.shared.details.ComponentDefinition;
import com.mgatelabs.ffbe.shared.details.DeviceDefinition;
import com.mgatelabs.ffbe.shared.details.ViewDefinition;
import com.mgatelabs.ffbe.shared.util.AdbShell;
import com.mgatelabs.ffbe.shared.util.AdbUtils;
import com.mgatelabs.ffbe.ui.utils.RefreshableListModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/29/2017.
 */
public class ComponentListPanel extends JInternalFrame {

    private DeviceDefinition deviceDefinition;
    private ViewDefinition viewDefinition;

    private final AdbShell shell;

    private JList<ComponentDefinition> itemList;
    private RefreshableListModel<ComponentDefinition> itemModel;

    public ComponentListPanel(DeviceDefinition deviceDefinition, ViewDefinition viewDefinition, AdbShell shell) {
        super("Components", true, false, false, false);
        this.deviceDefinition = deviceDefinition;
        this.viewDefinition = viewDefinition;
        this.shell = shell;

        build();
    }

    private void build() {

        GridBagConstraints c = new GridBagConstraints();

        setLayout(new GridBagLayout());

        itemModel = new RefreshableListModel<>(viewDefinition.getComponents());
        itemList = new JList<>(itemModel);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 5;
        c.weighty = 1.0f;
        c.weightx = 1.0f;
        JScrollPane scrollPane = new JScrollPane(itemList);
        this.add(scrollPane, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weighty = 0.0f;
        c.insets = new Insets(5, 5, 5, 5);
        this.add(buildForm(), c);
        pack();

        itemList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (itemList.getSelectedIndex() >= 0) {
                    selectedIndex = itemList.getSelectedIndex();
                    selectedItem = itemList.getSelectedValue();
                } else {
                    selectedItem = null;
                    selectedIndex = -1;
                }
                updateForm();
            }
        });

        JMenuBar menuBar = new JMenuBar();

        editMenu = new JMenu("Edit");
        editMenu.setEnabled(false);
        menuBar.add(editMenu);

        {
            JMenuItem saveMenuItem = new JMenuItem("Update Name");
            saveMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (nameField.getText() != null && nameField.getText().trim().length() > 0) {
                        selectedItem.setName(nameField.getText().trim());
                        viewDefinition.save();
                    } else {
                        nameField.setText(selectedItem.getName());
                    }
                }
            });
            editMenu.add(saveMenuItem);
        }

        testMenu = new JMenu("Test");
        testMenu.setEnabled(false);
        menuBar.add(testMenu);

        {
            JMenuItem tapMenuItem = new JMenuItem("Tap");
            tapMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    AdbUtils.component(deviceDefinition, selectedItem, ActionType.TAP, shell, false);
                }
            });
            testMenu.add(tapMenuItem);
        }

        testMenu.addSeparator();

        JMenuItem swipeUpMenuItem = new JMenuItem("Swipe Up");
        swipeUpMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AdbUtils.component(deviceDefinition, selectedItem, ActionType.SWIPE_UP, shell, false);
            }
        });
        testMenu.add(swipeUpMenuItem);

        JMenuItem swipeDownMenuItem = new JMenuItem("Swipe Down");
        swipeDownMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AdbUtils.component(deviceDefinition, selectedItem, ActionType.SWIPE_DOWN, shell, false);
            }
        });
        testMenu.add(swipeDownMenuItem);

        JMenuItem swipeLeftMenuItem = new JMenuItem("Swipe Left");
        swipeLeftMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AdbUtils.component(deviceDefinition, selectedItem, ActionType.SWIPE_LEFT, shell, false);
            }
        });
        testMenu.add(swipeLeftMenuItem);

        JMenuItem swipeRightMenuItem = new JMenuItem("Swipe Right");
        swipeRightMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AdbUtils.component(deviceDefinition, selectedItem, ActionType.SWIPE_RIGHT, shell, false);
            }
        });
        testMenu.add(swipeRightMenuItem);

        setJMenuBar(menuBar);

        pack();

        setVisible(true);
    }

    private void updateForm() {
        if (selectedItem != null) {
            idField.setText(selectedItem.getComponentId());
            nameField.setText(selectedItem.getName());
            nameField.setEnabled(true);
            editMenu.setEnabled(true);
            testMenu.setEnabled(true);
        } else {
            idField.setText("");
            nameField.setText("");
            nameField.setEnabled(false);
            editMenu.setEnabled(false);
            testMenu.setEnabled(false);
        }
    }

    private int selectedIndex;
    private ComponentDefinition selectedItem;
    private JTextField idField;
    private JTextField nameField;
    JMenu editMenu;
    JMenu testMenu;

    private JPanel buildForm() {
        JPanel panel = new JPanel();
        JLabel label;
        int y = 0;
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        label = new JLabel("ID");
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0;
        c.insets = new Insets(5, 5, 0, 5);
        panel.add(label, c);

        idField = new JTextField("");
        idField.setEnabled(false);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = y++;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        panel.add(idField, c);

        label = new JLabel("Name");
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0;
        panel.add(label, c);

        nameField = new JTextField("");
        nameField.setEnabled(false);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = y++;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        panel.add(nameField, c);

        return panel;
    }
}
