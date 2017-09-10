package com.mgatelabs.ffbe.shared.mapper;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/10/2017.
 */
public class FloorDefinition {

    private TileDefinition [][] floor;
    private String floorId;
    private int minX;
    private int minY;
    private int maxX;
    private int maxY;

    public FloorDefinition() {
        minX = 0;
        minY = 0;
        maxX = 0;
        maxY = 0;
    }

    public FloorDefinition(String floorId, int width, int height) {
        this();
        this.floorId = floorId;
        floor = new TileDefinition[height][width];
    }
}
