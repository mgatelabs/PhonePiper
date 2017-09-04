package com.mgatelabs.ffbe.shared.details;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/3/2017.
 */
public class ConditionDefinition {
    private ConditionType is;
    private String value;

    private ConditionDefinition and;
    private ConditionDefinition or;

    public ConditionType getIs() {
        return is;
    }

    public void setIs(ConditionType is) {
        this.is = is;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ConditionDefinition getAnd() {
        return and;
    }

    public void setAnd(ConditionDefinition and) {
        this.and = and;
    }

    public ConditionDefinition getOr() {
        return or;
    }

    public void setOr(ConditionDefinition or) {
        this.or = or;
    }
}
