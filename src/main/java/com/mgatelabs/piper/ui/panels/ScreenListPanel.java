package com.mgatelabs.piper.ui.panels;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mgatelabs.piper.shared.details.DeviceDefinition;
import com.mgatelabs.piper.shared.details.ScreenDefinition;
import com.mgatelabs.piper.shared.details.ViewDefinition;
import com.mgatelabs.piper.shared.helper.DeviceHelper;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.image.PngImageWrapper;
import com.mgatelabs.piper.shared.image.SamplePoint;
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
import java.util.List;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/29/2017.
 */
public class ScreenListPanel extends JInternalFrame {

    private DeviceDefinition deviceDefinition;
    private ViewDefinition viewDefinition;
    private final AdbShell shell;
    private final JFrame owner;

    private JList<ScreenDefinition> itemList;
    private RefreshableListModel<ScreenDefinition> itemModel;
    private DeviceHelper deviceHelper;

    public ScreenListPanel(DeviceHelper helper, DeviceDefinition deviceDefinition, ViewDefinition viewDefinition, AdbShell shell, JFrame owner) {
        super("Screens", true, false, false, false);
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

        listMenu = new JMenu("List");
        menuBar.add(listMenu);

        {
            JMenuItem newMenuItem = new JMenuItem("New");
            newMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String input = JOptionPane.showInputDialog(owner, "Screen ID (a-z A-Z 0-9 - _)");
                    if (input != null && Constants.ID_PATTERN.matcher(input).matches()) {

                        for (ScreenDefinition screenDefinition : viewDefinition.getScreens()) {
                            if (screenDefinition.getScreenId().equals(input)) {
                                info("Screen with same ID already exists");
                                return;
                            }
                        }

                        ScreenDefinition screenDefinition = new ScreenDefinition();
                        screenDefinition.setScreenId(input);
                        screenDefinition.setName(input);

                        ImageWrapper imageReader = AdbUtils.getScreen();

                        if (imageReader != null && imageReader.isReady()) {

                            List<SamplePoint> samples = new ArrayList<>();

                            ImagePixelPickerDialog imagePixelPickerDialog = new ImagePixelPickerDialog(ImagePixelPickerDialog.Mode.PIXELS, owner, null);
                            imagePixelPickerDialog.setup(imageReader, samples);
                            imagePixelPickerDialog.start();

                            if (!imagePixelPickerDialog.isOk()) {
                                return;
                            } else if (imagePixelPickerDialog.getPoints().isEmpty()) {
                                info("You did not select any samples");
                                return;
                            } else {
                                samples.addAll(imagePixelPickerDialog.getPoints());
                            }

                            screenDefinition.setPoints(samples);
                            screenDefinition.setEnabled(true);

                            imageReader.savePng(ScreenDefinition.getPreviewPath(viewDefinition.getViewId(), screenDefinition.getScreenId()));

                            deSelect();

                            viewDefinition.getScreens().add(screenDefinition);
                            viewDefinition.sort();
                            viewDefinition.save();

                            itemModel.refresh();
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
                    String input = JOptionPane.showInputDialog(owner, "Screen ID (a-z A-Z 0-9 - _)");
                    if (input != null && Constants.ID_PATTERN.matcher(input).matches()) {

                        for (ScreenDefinition screenDefinition : viewDefinition.getScreens()) {
                            if (screenDefinition.getScreenId().equals(input)) {
                                info("Screen with same ID already exists");
                                return;
                            }
                        }

                        ScreenDefinition screenDefinition = new ScreenDefinition();
                        screenDefinition.setScreenId(input);
                        screenDefinition.setName(input);

                        screenDefinition.setPoints(Lists.newArrayList());
                        screenDefinition.setEnabled(false);

                        deSelect();

                        viewDefinition.getScreens().add(screenDefinition);

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
                        selectedItem.setEnabled(true);
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
            JMenuItem testMenuItem = new JMenuItem("Update Image");
            testMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    AdbUtils.persistScreen(shell);
                    ImageWrapper wrapper = deviceHelper.download();
                    //ImageWrapper wrapper = AdbUtils.getScreen();
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

                    for (SamplePoint oldPoint : selectedItem.getPoints()) {
                        if (oldPoint.getX() >= deviceDefinition.getWidth() || oldPoint.getY() >= deviceDefinition.getHeight()) {
                            continue;
                        }
                        if (SamplePoint.validate(ImmutableList.of(oldPoint), imageWrapper, false)) {
                            newPoints.add(oldPoint);
                        }
                    }

                    ImagePixelPickerDialog imagePixelPickerDialog = new ImagePixelPickerDialog(ImagePixelPickerDialog.Mode.PIXELS, owner, null);
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

                    selectedItem.setEnabled(true);
                    selectedItem.getPoints().clear();
                    selectedItem.getPoints().addAll(newPoints);

                    viewDefinition.sort();
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

                    ImagePixelPickerDialog imagePixelPickerDialog = new ImagePixelPickerDialog(ImagePixelPickerDialog.Mode.PIXELS, owner, null);
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

                    selectedItem.setEnabled(true);
                    selectedItem.getPoints().clear();
                    selectedItem.getPoints().addAll(copy);
                    viewDefinition.sort();
                    viewDefinition.save();
                }
            });
            editMenu.add(updatePointsMenuItem);
        }

        editMenu.addSeparator();

        {
            JMenuItem saveMenuItem = new JMenuItem("Delete");
            saveMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (JOptionPane.showConfirmDialog(owner, "Are you sure, delete (" + selectedItem.getScreenId() + ")") == JOptionPane.YES_OPTION) {
                        viewDefinition.getScreens().remove(selectedIndex);

                        deSelect();

                        viewDefinition.sort();
                        viewDefinition.save();

                        itemModel.refresh();

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
            JMenuItem testMenuItem = new JMenuItem("Verify Live Image");
            testMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    AdbUtils.persistScreen(shell);
                    ImageWrapper wrapper = deviceHelper.download();
                    //ImageWrapper wrapper = AdbUtils.getScreen();
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

        itemModel.refresh();
    }

    private void deSelect() {
        selectedIndex = -1;
        selectedItem = null;
        itemList.clearSelection();
        updateForm();
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
