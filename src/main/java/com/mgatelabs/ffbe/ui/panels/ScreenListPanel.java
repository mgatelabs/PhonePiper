package com.mgatelabs.ffbe.ui.panels;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mgatelabs.ffbe.shared.details.ScreenDefinition;
import com.mgatelabs.ffbe.shared.details.ViewDefinition;
import com.mgatelabs.ffbe.shared.image.ImageWrapper;
import com.mgatelabs.ffbe.shared.image.PngImageWrapper;
import com.mgatelabs.ffbe.shared.image.SamplePoint;
import com.mgatelabs.ffbe.shared.util.AdbShell;
import com.mgatelabs.ffbe.shared.util.AdbUtils;
import com.mgatelabs.ffbe.ui.ImagePixelPickerDialog;
import com.mgatelabs.ffbe.ui.utils.RefreshableListModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/29/2017.
 */
public class ScreenListPanel extends JInternalFrame {

    private ViewDefinition viewDefinition;
    private final AdbShell shell;
    private final JFrame owner;

    private JList<ScreenDefinition> itemList;
    private RefreshableListModel<ScreenDefinition> itemModel;

    public ScreenListPanel(ViewDefinition viewDefinition, AdbShell shell, JFrame owner) {
        super("Screens", true, false, false, false);
        this.viewDefinition = viewDefinition;
        this.shell = shell;
        this.owner = owner;

        build();
    }

    private void build() {

        GridBagConstraints c = new GridBagConstraints();

        setLayout(new GridBagLayout());

        itemModel = new RefreshableListModel<>(viewDefinition.getScreens());
        itemList = new JList<>(itemModel);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridheight = 5;
        c.weighty = 1.0f;
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

        {
            JMenuItem testMenuItem = new JMenuItem("Update Image");
            testMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ImageWrapper wrapper = AdbUtils.getScreen();
                    if (wrapper != null && wrapper.isReady()) {
                        File previewPath = ScreenDefinition.getPreviewPath(viewDefinition.getViewId(), selectedItem.getScreenId());
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

        {
            JMenuItem testMenuItem = new JMenuItem("Fix Points");
            testMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ImageWrapper imageWrapper = PngImageWrapper.getPngImage(ScreenDefinition.getPreviewPath(viewDefinition.getViewId(), selectedItem.getScreenId()));

                    if (imageWrapper == null) {
                        info("Local preview missing");
                        return;
                    }

                    List<SamplePoint> newPoints = Lists.newArrayList();

                    for (SamplePoint oldPoint: selectedItem.getPoints()) {
                        if (SamplePoint.validate(ImmutableList.of(oldPoint), imageWrapper, false)) {
                            newPoints.add(oldPoint);
                        }
                    }

                    ImagePixelPickerDialog imagePixelPickerDialog = new ImagePixelPickerDialog(ImagePixelPickerDialog.Mode.PIXELS, owner);
                    imagePixelPickerDialog.setup(imageWrapper, newPoints);
                    imagePixelPickerDialog.start();

                    if (!imagePixelPickerDialog.isOk()) {
                        return;
                    }

                    if (imagePixelPickerDialog.getPoints().isEmpty()) {
                        info("Nothing selected");
                        return;
                    } else {
                        newPoints.clear();
                        newPoints.addAll(imagePixelPickerDialog.getPoints());
                    }

                    selectedItem.getPoints().clear();
                    selectedItem.getPoints().addAll(newPoints);

                    viewDefinition.save();

                }
            });
            editMenu.add(testMenuItem);
        }

        {
            JMenuItem updatePointsMenuItem = new JMenuItem("Update Points");
            updatePointsMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    ImageWrapper imageWrapper = PngImageWrapper.getPngImage(ScreenDefinition.getPreviewPath(viewDefinition.getViewId(), selectedItem.getScreenId()));

                    if (imageWrapper == null) {
                        info("Local preview missing");
                        return;
                    }

                    List<SamplePoint> copy = new ArrayList<>();
                    copy.addAll(selectedItem.getPoints());

                    ImagePixelPickerDialog imagePixelPickerDialog = new ImagePixelPickerDialog(ImagePixelPickerDialog.Mode.PIXELS, owner);
                    imagePixelPickerDialog.setup(imageWrapper, copy);
                    imagePixelPickerDialog.start();

                    if (!imagePixelPickerDialog.isOk()) {
                        return;
                    }

                    if (imagePixelPickerDialog.getPoints().isEmpty()) {
                        info("Nothing selected");
                        return;
                    } else {
                        copy.clear();
                        copy.addAll(imagePixelPickerDialog.getPoints());
                    }

                    selectedItem.getPoints().clear();
                    selectedItem.getPoints().addAll(copy);

                    viewDefinition.save();
                }
            });
            editMenu.add(updatePointsMenuItem);
        }

        testMenu = new JMenu("Test");
        testMenu.setEnabled(false);
        menuBar.add(testMenu);

        {
            JMenuItem testMenuItem = new JMenuItem("Verify Live Image");
            testMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ImageWrapper wrapper = AdbUtils.getScreen();
                    if (wrapper != null && wrapper.isReady()) {
                        if (SamplePoint.validate(selectedItem.getPoints(), wrapper, false)) {
                            info("Validation: Success");
                        } else {
                            info("Validation: Failed");
                        }
                    } else {
                        info("Could not obtain image");
                    }
                }
            });
            testMenu.add(testMenuItem);
        }

        {
            JMenuItem testMenuItem = new JMenuItem("Verify Saved Image");
            testMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    File previewPath = ScreenDefinition.getPreviewPath(viewDefinition.getViewId(), selectedItem.getScreenId());

                    ImageWrapper wrapper = PngImageWrapper.getPngImage(previewPath);

                    if (wrapper != null && wrapper.isReady()) {
                        if (SamplePoint.validate(selectedItem.getPoints(), wrapper, false)) {
                            info("Validation: Success");
                        } else {
                            info("Validation: Failed");
                        }
                    } else {
                        info("Could not obtain image");
                    }
                }
            });
            testMenu.add(testMenuItem);
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
            idField.setText(selectedItem.getScreenId());
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
    private ScreenDefinition selectedItem;
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
