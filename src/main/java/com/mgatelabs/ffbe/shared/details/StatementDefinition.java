package com.mgatelabs.ffbe.shared.details;

import java.util.List;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017.
 */
public class StatementDefinition {
    private ConditionDefinition condition;
    private List<ActionDefinition> actions;

    public ConditionDefinition getCondition() {
        return condition;
    }

    public void setCondition(ConditionDefinition condition) {
        this.condition = condition;
    }

    public List<ActionDefinition> getActions() {
        return actions;
    }

    public void setActions(List<ActionDefinition> actions) {
        this.actions = actions;
    }
}
