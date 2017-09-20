package com.mgatelabs.ffbe.ui.frame;

import com.google.common.collect.Lists;
import com.mgatelabs.ffbe.shared.details.PlayerDetail;
import com.mgatelabs.ffbe.ui.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 9/19/2017
 */
public class StartupFrame extends JFrame {

    final JFrame frame;
    private JComboBox<String> deviceComboBox;
    private JComboBox<String> viewComboBox;
    private JComboBox<String> scriptComboBox;
    private JComboBox<String> mapComboBox;
    private JComboBox<String> modeComboBox;
    private JComboBox<String> actionComboBox;
    private final PlayerDetail playerDetail;

    private String selectedView;
    private String selectedDevice;
    private String selectedScript;
    private String selectedMap;
    private String selectedMode;
    private String selectedAction;

    public StartupFrame(PlayerDetail playerDetail) throws HeadlessException {
        super("FFBExecute");
        frame = this;
        this.playerDetail = playerDetail;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        setMinimumSize(new Dimension(400, 200));
        build();
    }

    public String getSelectedDevice() {
        return selectedDevice;
    }

    public String getSelectedView() {
        return selectedView;
    }

    public String getSelectedScript() {
        return selectedScript;
    }

    public String getSelectedMap() {
        return selectedMap;
    }

    public String getSelectedMode() {
        return selectedMode;
    }

    public String getSelectedAction() {
        return selectedAction;
    }

    private boolean setup() {
        File devices = new File("./devices");
        File scripts = new File("./scripts");
        File views = new File("./views");
        return (devices.exists() && scripts.exists() && views.exists());
    }

    private void build() {

        if (!setup()) {

        } else {
            setLayout(new GridBagLayout());

            JPanel fieldPanel = new JPanel();
            fieldPanel.setLayout(new GridBagLayout());
            GridBagConstraints c2 = new GridBagConstraints();
            c2.insets = new Insets(5, 5, 5, 5);
            c2.gridwidth = 2;
            c2.fill = GridBagConstraints.HORIZONTAL;
            this.add(fieldPanel, c2);

            {
                GridBagConstraints c = new GridBagConstraints();

                // DEVICES

                JLabel label = new JLabel("Devices");
                c.gridx = 0;
                c.gridy = 0;
                c.ipadx = 4;
                c.weightx = 0;
                fieldPanel.add(label, c);

                deviceComboBox = new JComboBox<>(listJsonFilesIn(new File("./devices")));
                c.gridx = 1;
                c.gridy = 0;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weightx = 1;
                fieldPanel.add(deviceComboBox, c);

                // VIEWS

                label = new JLabel("Views");
                c.gridx = 0;
                c.gridy = 1;
                c.ipadx = 4;
                c.weightx = 0;
                fieldPanel.add(label, c);

                viewComboBox = new JComboBox<>(listFoldersFilesIn(new File("./views")));
                c.gridx = 1;
                c.gridy = 1;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weightx = 1;
                fieldPanel.add(viewComboBox, c);

                // SCRIPTS

                label = new JLabel("Scripts");
                c.gridx = 0;
                c.gridy = 2;
                c.ipadx = 4;
                c.weightx = 0;
                fieldPanel.add(label, c);

                scriptComboBox = new JComboBox<>(listJsonFilesIn(new File("./scripts")));

                c.gridx = 1;
                c.gridy = 2;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weightx = 1;
                fieldPanel.add(scriptComboBox, c);

                // MAPS

                label = new JLabel("Maps");
                c.gridx = 0;
                c.gridy = 3;
                c.ipadx = 4;
                c.weightx = 0;
                fieldPanel.add(label, c);

                mapComboBox = new JComboBox<>(listJsonFilesIn(new File("./maps")));
                c.gridx = 1;
                c.gridy = 3;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weightx = 1;
                fieldPanel.add(mapComboBox, c);

                // MODE

                label = new JLabel("Mode");
                c.gridx = 0;
                c.gridy = 4;
                c.ipadx = 4;
                c.weightx = 0;
                fieldPanel.add(label, c);

                String[] modeArray = new String[]{Constants.MODE_SCRIPT, Constants.MODE_MAP, Constants.MODE_DEVICE};
                modeComboBox = new JComboBox<>(modeArray);
                c.gridx = 1;
                c.gridy = 4;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weightx = 1;
                fieldPanel.add(modeComboBox, c);

                // ACTION

                label = new JLabel("Action");
                c.gridx = 0;
                c.gridy = 5;
                c.ipadx = 4;
                c.weightx = 0;
                fieldPanel.add(label, c);

                String[] actionArray = new String[]{Constants.ACTION_RUN, Constants.ACTION_EDIT, Constants.ACTION_CREATE, Constants.ACTION_DELETE};
                actionComboBox = new JComboBox<>(actionArray);
                c.gridx = 1;
                c.gridy = 5;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weightx = 1;
                fieldPanel.add(actionComboBox, c);

            }

            // Actions

            JButton close = new JButton("Close");
            close.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectedAction = null;
                    selectedMode = null;
                    frame.dispose();
                }
            });
            c2.gridx = 0;
            c2.gridy = 1;
            c2.gridwidth = 1;
            c2.fill = GridBagConstraints.HORIZONTAL;
            c2.weightx = 1;
            c2.insets = new Insets(10, 5, 10, 5);
            this.add(close, c2);

            JButton go = new JButton("Start");
            go.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectedScript = (String) scriptComboBox.getSelectedItem();
                    selectedDevice = (String) deviceComboBox.getSelectedItem();
                    selectedView = (String) viewComboBox.getSelectedItem();
                    selectedMap = (String) mapComboBox.getSelectedItem();
                    selectedAction = (String) actionComboBox.getSelectedItem();
                    selectedMode = (String) modeComboBox.getSelectedItem();
                    frame.dispose();
                }
            });
            c2.gridx = 1;
            c2.gridy = 1;
            this.add(go, c2);

        }

        pack();
        setVisible(true);
    }

    private String[] listJsonFilesIn(File dir) {
        List<String> itemList = Lists.newArrayList();
        for (File f : dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".json");
            }
        })) {
            itemList.add(f.getName().substring(0, f.getName().length() - 5));
        }
        Collections.sort(itemList);
        String[] itemArray = new String[itemList.size()];
        itemList.toArray(itemArray);
        return itemArray;
    }


    private String[] listFoldersFilesIn(File dir) {
        List<String> itemList = Lists.newArrayList();
        for (File f : dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        })) {
            itemList.add(f.getName());
        }
        Collections.sort(itemList);
        String[] itemArray = new String[itemList.size()];
        itemList.toArray(itemArray);
        return itemArray;
    }
}
