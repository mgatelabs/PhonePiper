package com.mgatelabs.piper.shared.mapper;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/10/2017 for Phone-Piper
 */
public class TileEvent {
    private String floorId;
    private int triggerX;
    private int triggerY;
    private int destX;
    private int dextY;
    private TileEventType type;

    public TileEvent() {
    }

    public TileEvent(String floorId, TileEventType type, int triggerX, int triggerY, int destX, int dextY) {
        this.floorId = floorId;
        this.triggerX = triggerX;
        this.triggerY = triggerY;
        this.destX = destX;
        this.dextY = dextY;
        this.type = type;
    }

    public String getFloorId() {
        return floorId;
    }

    public void setFloorId(String floorId) {
        this.floorId = floorId;
    }

    public int getTriggerX() {
        return triggerX;
    }

    public void setTriggerX(int triggerX) {
        this.triggerX = triggerX;
    }

    public int getTriggerY() {
        return triggerY;
    }

    public void setTriggerY(int triggerY) {
        this.triggerY = triggerY;
    }

    public int getDestX() {
        return destX;
    }

    public void setDestX(int destX) {
        this.destX = destX;
    }

    public int getDextY() {
        return dextY;
    }

    public void setDextY(int dextY) {
        this.dextY = dextY;
    }

    public TileEventType getType() {
        return type;
    }

    public void setType(TileEventType type) {
        this.type = type;
    }
}
