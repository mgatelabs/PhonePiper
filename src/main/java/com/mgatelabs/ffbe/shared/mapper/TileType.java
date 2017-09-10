package com.mgatelabs.ffbe.shared.mapper;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/10/2017.
 */
public enum TileType {
    UNKNOWN('?'),
    START('S'),
    EMPTY('X'),
    FLOOR('_'),
    WALL('#'),
    DOOR('@'),
    ENCOUNTER('E'),
    EXIT('X');

    private final char marker;

    TileType(char marker) {
        this.marker = marker;
    }

    public char getMarker() {
        return marker;
    }
}
