package com.mgatelabs.ffbe.shared.image;

import java.util.Map;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/6/2017.
 */
public class InfoTransfer {
    Map<String, StateTransfer> states;

    public Map<String, StateTransfer> getStates() {
        return states;
    }

    public void setStates(Map<String, StateTransfer> states) {
        this.states = states;
    }
}
