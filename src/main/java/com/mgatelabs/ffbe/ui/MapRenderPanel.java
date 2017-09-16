package com.mgatelabs.ffbe.ui;

import com.mgatelabs.ffbe.shared.mapper.FloorDefinition;
import com.mgatelabs.ffbe.shared.mapper.MapDefinition;
import com.mgatelabs.ffbe.shared.mapper.MapSampleArea;
import com.mgatelabs.ffbe.shared.mapper.TileDefinition;

import javax.swing.*;
import java.awt.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/10/2017.
 */
public class MapRenderPanel extends JPanel {

    private MapDefinition mapDefinition;

    private int viewX;
    private int viewY;

    private String floorId;
    private FloorDefinition floorDefinition;

    private int sampleX;
    private int sampleY;
    private MapSampleArea mapSampleArea;

    public MapRenderPanel() {
        floorDefinition = null;
        floorId = null;
        mapSampleArea = null;

        viewX = 0;
        viewY = 0;

        sampleX = 0;
        sampleY = 0;
    }

    public MapDefinition getMapDefinition() {
        return mapDefinition;
    }

    public void setMapDefinition(MapDefinition mapDefinition) {
        this.mapDefinition = mapDefinition;
    }

    public void setFloor(String floorId) {

        // Clean first

        this.floorId = null;
        this.floorDefinition = null;

        if (mapDefinition != null) {
            FloorDefinition definition = mapDefinition.getFloors().get(floorId);
            if (definition != null) {
                this.floorId = floorId;
                this.floorDefinition = definition;
            }
        }

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if (floorDefinition != null) {
            int possibleWidth = getWidth() / 20;
            int possibleHeight = getHeight() / 20;
            if (possibleWidth > 1 && possibleHeight > 1) {

                final int maxW = floorDefinition.getWidth();
                final int maxH = floorDefinition.getHeight();

                for (int y = 0; y < possibleHeight; y++)
                    for (int x = 0; x < possibleWidth; x++) {
                        final int startX = x * 20;
                        final int startY = y * 20;
                        int offx = viewX + x;
                        int offy = viewY + y;
                        Color border = Color.gray;
                        if (offx >= 0 && offx < maxW && offy >= 0 && offy < maxH) {

                            TileDefinition tileDefinition = floorDefinition.getTile(offx, offy);
                            Color c = Color.YELLOW;
                            if (tileDefinition == null) {

                            } else {
                                switch (floorDefinition.getTile(offx, offy).getType()) {
                                    case UNKNOWN: {

                                    }
                                    break;
                                    case FLOOR: {
                                        c = Color.blue;
                                    }
                                    break;
                                    case EXIT: {
                                        c = Color.red;
                                    }
                                    break;
                                    case DOOR: {
                                        c = Color.CYAN;
                                    }
                                    break;
                                    case ENCOUNTER: {
                                        c = Color.orange;
                                    }
                                    break;
                                    case START: {
                                        c = Color.green;
                                    }
                                    break;
                                    case EMPTY: {
                                        c = Color.gray;
                                        border = Color.white;
                                    }
                                    break;
                                    case WALL: {
                                        c = Color.magenta;
                                    }
                                    break;
                                    case FALSEFLOOR: {
                                        c = Color.white;
                                    }
                                    break;
                                }
                            }
                            g2d.setColor(c);
                            g2d.fillRect(startX, startY, 20, 20);
                        } else {
                            g2d.setColor(Color.BLACK);
                            g2d.fillRect(startX, startY, 20, 20);
                        }
                        g2d.setColor(border);
                        g2d.drawRect(startX + 1, startY + 1, 20 - 2, 20 - 2);
                    }
            } else {
                // To small, cancel out
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        } else {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
