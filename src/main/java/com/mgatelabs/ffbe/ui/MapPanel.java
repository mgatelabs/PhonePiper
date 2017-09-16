package com.mgatelabs.ffbe.ui;

import com.mgatelabs.ffbe.shared.mapper.FloorDefinition;
import com.mgatelabs.ffbe.shared.mapper.MapDefinition;

import javax.swing.*;
import java.awt.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/15/2017.
 */
public class MapPanel extends JInternalFrame {

    MapRenderPanel mapRenderPanel;

    public MapPanel() {
        super("Map");

        setMinimumSize(new Dimension(620, 480));
        setPreferredSize(getMinimumSize());

        setResizable(true);
        setClosable(false);
        setMaximizable(true);
        setIconifiable(false);

        mapRenderPanel = new MapRenderPanel();

        add(mapRenderPanel);

        pack();

        setVisible(true);
    }

    public void setMap(MapDefinition map) {
        mapRenderPanel.setMapDefinition(map);
        // look for the default
        mapRenderPanel.setFloor("start");
    }

}
