package com.mgatelabs.ffbe.shared.details;

import java.util.List;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017.
 */
public class StatementDetail {
    private ConditionDetail condition;
    private List<ActionDetail> actions;

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
