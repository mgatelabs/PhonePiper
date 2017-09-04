package com.mgatelabs.ffbe.shared.details;

import java.util.List;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017.
 */
public class StateDetail {
    private String name;
    private ConditionDetail condition;
    private List<ActionDetail> actions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ConditionDetail getCondition() {
        return condition;
    }

    public void setCondition(ConditionDetail condition) {
        this.condition = condition;
    }

    public List<ActionDetail> getActions() {
        return actions;
    }

    public void setActions(List<ActionDetail> actions) {
        this.actions = actions;
    }
}
