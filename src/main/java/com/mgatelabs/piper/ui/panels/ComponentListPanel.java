package com.mgatelabs.piper.ui.panels;

import com.mgatelabs.piper.shared.details.ActionType;
import com.mgatelabs.piper.shared.details.ComponentDefinition;
import com.mgatelabs.piper.shared.details.DeviceDefinition;
import com.mgatelabs.piper.shared.details.ViewDefinition;
import com.mgatelabs.piper.shared.helper.DeviceHelper;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.image.PngImageWrapper;
import com.mgatelabs.piper.shared.util.AdbShell;
import com.mgatelabs.piper.shared.util.AdbUtils;
import com.mgatelabs.piper.ui.dialogs.ImagePixelPickerDialog;
import com.mgatelabs.piper.ui.utils.Constants;
import com.mgatelabs.piper.ui.utils.RefreshableListModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/29/2017.
 */
public class ComponentListPanel extends JInternalFrame {

    private DeviceDefinition deviceDefinition;
    private ViewDefinition viewDefinition;
    private final JFrame owner;

    private final AdbShell shell;

    private JList<ComponentDefinition> itemList;
    private RefreshableListModel<ComponentDefinition> itemModel;
    private DeviceHelper deviceHelper;

    public ComponentListPanel(DeviceHelper helper, DeviceDefinition deviceDefinition, ViewDefinition viewDefinition, AdbShell shell, JFrame owner) {
        super("Components", true, false, false, false);
        this.deviceDefinition = deviceDefinition;
        this.viewDefinition = viewDefinition;
        this.shell = shell;
        this.owner = owner;
        deviceHelper = helper;
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

        listMenu = new JMenu("List");
        menuBar.add(listMenu);


        {
            JMenuItem newMenuItem = new JMenuItem("New");
            newMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String input = JOptionPane.showInputDialog(owner, "Component ID (a-z A-Z 0-9 - _)");
                    if (input != null && Constants.ID_PATTERN.matcher(input).matches()) {

                        for (ComponentDefinition componentDefinition : viewDefinition.getComponents()) {
                            if (componentDefinition.getComponentId().equals(input)) {
                                info("Screen with same ID already exists");
                                return;
                            }
                        }

                        ComponentDefinition componentDefinition = new ComponentDefinition();
                        componentDefinition.setComponentId(input);
                        componentDefinition.setName(input);

                        ImageWrapper imageReader = AdbUtils.getScreen();

                        if (imageReader != null && imageReader.isReady()) {

                            ImagePixelPickerDialog imagePixelPickerDialog = new ImagePixelPickerDialog(ImagePixelPickerDialog.Mode.BOX, owner, null);
                            imagePixelPickerDialog.setup(imageReader, new ArrayList<>());
                            imagePixelPickerDialog.start();

                            if (!imagePixelPickerDialog.isOk()) {
                                return;
                            } else if (imagePixelPickerDialog.getPoints().isEmpty()) {
                                info("You did not select any samples, try again: (y/n)");
                                return;
                            } else if (imagePixelPickerDialog.getPoints().size() != 2) {
                                info("You must select 2 points");
                                return;
                            } else {

                                int x1 = imagePixelPickerDialog.getPoints().get(0).getX();
                                int x2 = imagePixelPickerDialog.getPoints().get(1).getX();
                                int y1 = imagePixelPickerDialog.getPoints().get(0).getY();
                                int y2 = imagePixelPickerDialog.getPoints().get(1).getY();

                                if (x1 > x2) {
                                    int temp = x1;
                                    x1 = x2;
                                    x2 = temp;
                                }

                                if (y1 > y2) {
                                    int temp = y1;
                                    y1 = y2;
                                    y2 = temp;
                                }

                                componentDefinition.setX(x1);
                                componentDefinition.setY(y1);
                                componentDefinition.setW(x2 - x1);
                                componentDefinition.setH(y2 - y1);
                                componentDefinition.setEnabled(true);

                                imageReader.savePng(ComponentDefinition.getPreviewPath(deviceDefinition.getViewId(), componentDefinition.getComponentId()));

                                selectedIndex = -1;
                                selectedItem = null;
                                itemList.clearSelection();
                                updateForm();

                                viewDefinition.getComponents().add(componentDefinition);

                                viewDefinition.sort();

                                viewDefinition.save();

                                itemModel.refresh();
                            }

                        }
                    }
                }
            });
            listMenu.add(newMenuItem);
        }

        {
            JMenuItem newMenuItem = new JMenuItem("Stub");
            newMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String input = JOptionPane.showInputDialog(owner, "Component ID (a-z A-Z 0-9 - _)");
                    if (input != null && Constants.ID_PATTERN.matcher(input).matches()) {

                        for (ComponentDefinition componentDefinition : viewDefinition.getComponents()) {
                            if (componentDefinition.getComponentId().equals(input)) {
                                info("Screen with same ID already exists");
                                return;
                            }
                        }

                        ComponentDefinition componentDefinition = new ComponentDefinition();
                        componentDefinition.setComponentId(input);
                        componentDefinition.setName(input);

                        componentDefinition.setX(0);
                        componentDefinition.setY(0);
                        componentDefinition.setW(1);
                        componentDefinition.setH(1);
                        componentDefinition.setEnabled(false);

                        selectedIndex = -1;
                        selectedItem = null;
                        itemList.clearSelection();
                        updateForm();

                        viewDefinition.getComponents().add(componentDefinition);

                        viewDefinition.sort();

                        viewDefinition.save();

                        itemModel.refresh();
                    }
                }
            });
            listMenu.add(newMenuItem);
        }

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
                        viewDefinition.sort();
                        viewDefinition.save();
                        itemModel.refresh();
                    } else {
                        nameField.setText(selectedItem.getName());
                    }
                }
            });
            editMenu.add(saveMenuItem);
        }

        {
            JMenuItem updatePointsMenuItem = new JMenuItem("Update Points");
            updatePointsMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    ImageWrapper imageWrapper = PngImageWrapper.getPngImage(ComponentDefinition.getPreviewPath(viewDefinition.getViewId(), selectedItem.getComponentId()));

                    if (imageWrapper == null) {
                        info("Local preview missing");
                        return;
                    }

                    ImagePixelPickerDialog imagePixelPickerDialog = new ImagePixelPickerDialog(ImagePixelPickerDialog.Mode.BOX, null, null);

                    int x = selectedItem.getX() >= deviceDefinition.getWidth() ? 0 : selectedItem.getX();
                    int y = selectedItem.getY() >= deviceDefinition.getHeight() ? 0 : selectedItem.getY();

                    int w = (x + selectedItem.getW()) >= deviceDefinition.getWidth() ? 10 : selectedItem.getW();
                    int h = (y + selectedItem.getH()) >= deviceDefinition.getHeight() ? 10 : selectedItem.getH();

                    imagePixelPickerDialog.setup(imageWrapper, x, y, w, h);
                    imagePixelPickerDialog.start();

                    if (!imagePixelPickerDialog.isOk()) {
                        System.out.println("Stopping");
                        return;
                    }

                    if (imagePixelPickerDialog.getPoints().isEmpty()) {
                        info("You did not select any samples");
                        return;
                    } else if (imagePixelPickerDialog.getPoints().size() != 2) {
                        info("You must select 2 points");
                    } else {

                        int x1 = imagePixelPickerDialog.getPoints().get(0).getX();
                        int x2 = imagePixelPickerDialog.getPoints().get(1).getX();
                        int y1 = imagePixelPickerDialog.getPoints().get(0).getY();
                        int y2 = imagePixelPickerDialog.getPoints().get(1).getY();

                        if (x1 > x2) {
                            int temp = x1;
                            x1 = x2;
                            x2 = temp;
                        }

                        if (y1 > y2) {
                            int temp = y1;
                            y1 = y2;
                            y2 = temp;
                        }

                        selectedItem.setEnabled(true);

                        selectedItem.setX(x1);
                        selectedItem.setY(y1);
                        selectedItem.setW(x2 - x1);
                        selectedItem.setH(y2 - y1);

                        viewDefinition.sort();
                        info(viewDefinition.save() ? "Saved" : "Failed");
                    }
                }
            });
            editMenu.add(updatePointsMenuItem);
        }

        {
            JMenuItem testMenuItem = new JMenuItem("Update Image");
            testMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    AdbUtils.persistScreen(shell);
                    ImageWrapper wrapper = deviceHelper.download();
                    //ImageWrapper wrapper = AdbUtils.getScreen();
                    if (wrapper != null && wrapper.isReady()) {
                        File previewPath = ComponentDefinition.getPreviewPath(viewDefinition.getViewId(), selectedItem.getComponentId());
                        if (!wrapper.savePng(previewPath)) {
                            info("Failed to update image");
                        } else {
                            info("Image Updated");
                        }
                    } else {
                        info("Could not obtain image from device");
                    }
                }
            });
            editMenu.add(testMenuItem);
        }

        editMenu.addSeparator();

        {
            JMenuItem saveMenuItem = new JMenuItem("Disable");
            saveMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectedItem.setEnabled(false);
                    viewDefinition.sort();
                    viewDefinition.save();
                    itemModel.refresh();
                }
            });
            editMenu.add(saveMenuItem);
        }

        editMenu.addSeparator();

        {
            JMenuItem saveMenuItem = new JMenuItem("Delete");
            saveMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (JOptionPane.showConfirmDialog(owner, "Are you sure, delete (" + selectedItem.getComponentId() + ")") == JOptionPane.YES_OPTION) {
                        viewDefinition.getComponents().remove(selectedIndex);
                        itemList.clearSelection();
                        selectedItem = null;
                        selectedIndex = -1;
                        updateForm();
                        viewDefinition.sort();
                        viewDefinition.save();
                        info("Deleted");
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

        {
            JMenuItem swipeUpMenuItem = new JMenuItem("Swipe Up");
            swipeUpMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    AdbUtils.component(deviceDefinition, selectedItem, ActionType.SWIPE_UP, shell, false);
                }
            });
            testMenu.add(swipeUpMenuItem);
        }

        {
            JMenuItem swipeSlowUpMenuItem = new JMenuItem("Swipe Up (Slow)");
            swipeSlowUpMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    AdbUtils.component(deviceDefinition, selectedItem, ActionType.SLOW_UP, shell, false);
                }
            });
            testMenu.add(swipeSlowUpMenuItem);
        }

        {
            JMenuItem swipeDownMenuItem = new JMenuItem("Swipe Down");
            swipeDownMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    AdbUtils.component(deviceDefinition, selectedItem, ActionType.SWIPE_DOWN, shell, false);
                }
            });
            testMenu.add(swipeDownMenuItem);
        }

        {
            JMenuItem swipeSlowDownMenuItem = new JMenuItem("Swipe Down (Slow)");
            swipeSlowDownMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    AdbUtils.component(deviceDefinition, selectedItem, ActionType.SLOW_DOWN, shell, false);
                }
            });
            testMenu.add(swipeSlowDownMenuItem);
        }

        {
            JMenuItem swipeLeftMenuItem = new JMenuItem("Swipe Left");
            swipeLeftMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    AdbUtils.component(deviceDefinition, selectedItem, ActionType.SWIPE_LEFT, shell, false);
                }
            });
            testMenu.add(swipeLeftMenuItem);
        }

        {
            JMenuItem swipeLeftMenuItem = new JMenuItem("Swipe Left (Slow)");
            swipeLeftMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    AdbUtils.component(deviceDefinition, selectedItem, ActionType.SLOW_LEFT, shell, false);
                }
            });
            testMenu.add(swipeLeftMenuItem);
        }

        {
            JMenuItem swipeRightMenuItem = new JMenuItem("Swipe Right");
            swipeRightMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    AdbUtils.component(deviceDefinition, selectedItem, ActionType.SWIPE_RIGHT, shell, false);
                }
            });
            testMenu.add(swipeRightMenuItem);
        }

        {
            JMenuItem swipeRightMenuItem = new JMenuItem("Swipe Right (Slow)");
            swipeRightMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    AdbUtils.component(deviceDefinition, selectedItem, ActionType.SLOW_RIGHT, shell, false);
                }
            });
            testMenu.add(swipeRightMenuItem);
        }

        setJMenuBar(menuBar);

        pack();

        setVisible(true);
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg);
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
    JMenu listMenu;
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
