package com.mgatelabs.ffbe.shared.details;

import java.util.List;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/3/2017.
 */
public class ConditionDetail {
    private ConditionType type;
    private String value;

    private List<ConditionDetail> and;
    private List<ConditionDetail> or;

    public ConditionType getType() {
        return type;
    }

    public void setType(ConditionType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<ConditionDetail> getAnd() {
        return and;
    }

    public void setAnd(List<ConditionDetail> and) {
        this.and = and;
    }

    public List<ConditionDetail> getOr() {
        return or;
    }

    public void setOr(List<ConditionDetail> or) {
        this.or = or;
    }
}
