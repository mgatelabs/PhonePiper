package com.mgatelabs.ffbe.ui;

import com.google.common.collect.Lists;
import com.mgatelabs.ffbe.shared.mapper.FloorDefinition;
import com.mgatelabs.ffbe.shared.mapper.MapSampleArea;

import javax.swing.*;
import java.awt.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/17/2017.
 */
public class MapperPanel extends JInternalFrame {

    MapPanel mapPanel;

    JList<FloorDefinition> floorList;
    RefreshableListModel<FloorDefinition> floorModel;

    JList<MapSampleArea> sampleList;
    RefreshableListModel<MapSampleArea> sampleModel;

    JButton captureSample;
    JButton applySample;
    JButton withdrawSample;
    JButton deleteSample;

    public MapperPanel(MapPanel mapPanel) {
        super("Mapper", true, false, false, false);
        this.mapPanel = mapPanel;


        setMinimumSize(new Dimension(300, 450));
        setPreferredSize(getMinimumSize());

        JPanel container = new JPanel();
        container.setLayout(new GridBagLayout());
        this.add(container);

        GridBagConstraints c = new GridBagConstraints();

        JLabel floorLabel = new JLabel("Floors");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        c.gridheight = 1;
        container.add(floorLabel, c);

        floorModel = new RefreshableListModel<>(Lists.newArrayList());
        floorList = new JList<>(floorModel);
        JScrollPane floorScroller = new JScrollPane(floorList);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        c.weighty = 1.0;
        c.gridwidth = 4;
        c.gridheight = 4;
        container.add(floorScroller, c);


        JLabel sampleLabel = new JLabel("Samples");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 6;
        c.weighty = 0.0;
        c.gridwidth = 4;
        c.gridheight =1;
        container.add(sampleLabel, c);

        sampleModel = new RefreshableListModel<>(Lists.newArrayList());
        sampleList = new JList<>(sampleModel);
        JScrollPane sampleScroller = new JScrollPane(sampleList);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 7;
        c.weighty = 1.0;
        c.gridwidth = 4;
        c.gridheight = 4;
        container.add(sampleScroller, c);

        captureSample = new JButton("Capture");
        captureSample.setToolTipText("Capture a sample from your device");
        c.weightx = 0.25;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 11;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weighty = 0;
        container.add(captureSample, c);

        applySample = new JButton("=");
        applySample.setToolTipText("Apply selected sample to make");
        c.weightx = 0.25;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridwidth = 1;
        container.add(applySample, c);

        withdrawSample = new JButton("Store");
        withdrawSample.setToolTipText("Un-select current sample");
        c.weightx = 0.25;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridwidth = 1;
        container.add(withdrawSample, c);

        deleteSample = new JButton("-");
        deleteSample.setToolTipText("Delete selected sample");
        c.weightx = 0.25;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.gridwidth = 1;
        container.add(deleteSample, c);

        this.pack();
        this.setVisible(true);
    }
}
