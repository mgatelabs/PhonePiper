package com.mgatelabs.piper.shared.mapper;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/10/2017.
 */
public class TileDefinition {
    private short neighborCode;
    private TileType type;

    public TileDefinition(short neighborCode, TileType type) {
        this.neighborCode = neighborCode;
        this.type = type;
    }

    public TileDefinition() {
        type = TileType.UNKNOWN;
    }

    public short getNeighborCode() {
        return neighborCode;
    }

    public void setNeighborCode(short neighborCode) {
        this.neighborCode = neighborCode;
    }

    public TileType getType() {
        return type;
    }

    public void setType(TileType type) {
        this.type = type;
    }

    public static short calculate(TileDefinition[][] map, int x, int y) {
        short value = 0;
        int pos = 1;
        for (int Y = -1; Y <= 1; Y++) {
            for (int X = -1; X <= 1; X++) {
                int ox = x + X;
                int oy = y + Y;
                if (ox >= 0 && ox < map[y].length && oy >= 0 && oy < map.length) {
                    if (map[oy][ox].getType() == TileType.UNKNOWN) {
                        // If we touch the unknown, don't calculate, too much trouble, save it for later
                        return -1;
                    }
                   if (map[oy][ox].getType().isWalkable()) {
                       value += pos;
                   }
                } else {
                    return -1;
                }
                pos <<= 1;
            }
        }
        return value;
    }
}
