package com.mgatelabs.ffbe.shared.mapper;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/10/2017.
 */
public class TileDefinition {
    private short neighborCode;
    private TileType type;

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
}
