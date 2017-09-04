package com.mgatelabs.ffbe.shared.details;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/3/2017.
 */
public class ActionDefinition {
    private ActionType type;
    private String value;

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
