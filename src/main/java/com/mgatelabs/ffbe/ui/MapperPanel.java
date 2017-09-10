package com.mgatelabs.ffbe.ui;

import com.mgatelabs.ffbe.shared.mapper.MapDefinition;

import javax.swing.*;
import java.awt.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/10/2017.
 */
public class MapperPanel extends JPanel {

    private MapDefinition mapDefinition;

    public MapperPanel() {
    }

    public MapDefinition getMapDefinition() {
        return mapDefinition;
    }

    public void setMapDefinition(MapDefinition mapDefinition) {
        this.mapDefinition = mapDefinition;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

    }
}
