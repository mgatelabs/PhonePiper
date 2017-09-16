package com.mgatelabs.ffbe.shared.mapper;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/10/2017.
 */
public class MapDefinition {

    private String mapId;
    private Map<String, FloorDefinition> floors;
    private Map<String, TileEvent> events;

    public MapDefinition() {
        mapId = "unknown";
        floors = Maps.newHashMap();
        events = Maps.newHashMap();
    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
    }

    public Map<String, FloorDefinition> getFloors() {
        return floors;
    }

    public void setFloors(Map<String, FloorDefinition> floors) {
        this.floors = floors;
    }

    public FloorDefinition addFloor(String floorId) {
        FloorDefinition floorDefinition = floors.get(floorId);
        if (floorDefinition == null) {
            floorDefinition = new FloorDefinition(floorId, 512, 512);
            floors.put(floorId, floorDefinition);
        }
        return floorDefinition;
    }

    public FloorDefinition getFloor(String floorId) {
        return floors.get(floorId);
    }

    public Map<String, TileEvent> getEvents() {
        return events;
    }

    public void setEvents(Map<String, TileEvent> events) {
        this.events = events;
    }
}
