package com.mgatelabs.ffbe.shared.details;

import java.util.Map;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017.
 */
public class ScriptDetail {

    Map<String, StateDetail> states;

    public Map<String, StateDetail> getStates() {
        return states;
    }

    public void setStates(Map<String, StateDetail> states) {
        this.states = states;
    }
}
