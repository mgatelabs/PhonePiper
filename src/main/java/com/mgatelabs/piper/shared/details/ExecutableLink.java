package com.mgatelabs.piper.shared.details;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Wrapper for a StateLink that allows children variables to be compiled into one place
 *
 * Created by @mgatelabs (Michael Fuller) on 10/13/2018 for Phone-Piper.
 */
public class ExecutableLink {
    private final StateLink link;
    private final Map<String, VarDefinition> variables;
    private boolean valid;

    public ExecutableLink(StateLink link) {
        this.link = link;
        // Collect all variable definitions
        variables = ImmutableMap.copyOf(link.getVariableDefinitions());
    }

    public StateType getType() {
        return link.getType();
    }

    public StateLink getLink() {
        return link;
    }

    public String getId() {
        return link.getState().getId();
    }

    public String getName() {
        return link.getState().getName();
    }

    public Map<String, VarDefinition> getVariables() {
        return variables;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public String toString() {
        return link.getState().getId();
    }
}
