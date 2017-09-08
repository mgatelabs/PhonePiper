package com.mgatelabs.ffbe.shared.helper;

import com.mgatelabs.ffbe.shared.image.StateTransfer;

import java.util.Map;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/6/2017.
 */
public class InfoTransfer {
    private Map<String, StateTransfer> states;

    private MapTransfer map;

    public Map<String, StateTransfer> getStates() {
        return states;
    }

    public void setStates(Map<String, StateTransfer> states) {
        this.states = states;
    }

    public MapTransfer getMap() {
        return map;
    }

    public void setMap(MapTransfer map) {
        this.map = map;
    }
}
