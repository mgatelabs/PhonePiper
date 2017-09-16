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

    public String getFloorId() {
        return floorId;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getWidth() {
        return floor != null ? floor[0].length : 0;
    }

    public int getHeight() {
        return floor != null ? floor.length : 0;
    }

    public TileDefinition getTile(int x, int y) {
        if (y >= 0 && y < floor.length && x >= 0 && x < floor[y].length) {
            return floor[y][x];
        }
        return null;
    }

    public TileDefinition setTile(int x, int y, TileDefinition tileDefinition) {
        if (y >= 0 && y < floor.length && x >= 0 && x < floor[y].length) {
            floor[y][x] = tileDefinition;
            TileDefinition.calculate(floor, x, y);
        }
        return null;
    }
}
