package com.mgatelabs.piper.shared.details;

import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017 for Phone-Piper
 */
public class StatementDefinition {
    private ConditionDefinition condition;
    private String description;
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

    public String getDescription() {
        return description == null ? "" : description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> determineScreenIds(final Set<String> exploredStates, final Map<String, ExecutableLink> states) {
        final Set<String> found = Sets.newHashSet();
        if (getCondition() != null) {
            found.addAll(getCondition().determineScreenIds(exploredStates, states));
        }
        for (ActionDefinition actionDefinition : actions) {
            found.addAll(actionDefinition.determineScreenIds(exploredStates, states));
        }
        return found;
    }
}
