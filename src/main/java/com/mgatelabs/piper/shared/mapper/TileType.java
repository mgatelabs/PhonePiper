package com.mgatelabs.piper.shared.mapper;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/10/2017.
 */
public enum TileType {
    UNKNOWN('?', false),
    START('S', true),
    EMPTY('X', false),
    FLOOR('_', true),
    FALSEFLOOR('*', true),
    WALL('#', false),
    DOOR('@', true),
    ENCOUNTER('E', true),
    EXIT('X', true);

    private final char marker;
    private final boolean walkable;

    TileType(char marker, boolean walkable) {
        this.marker = marker;
        this.walkable = walkable;
    }

    public boolean isWalkable() {
        return walkable;
    }

    public char getMarker() {
        return marker;
    }
}
