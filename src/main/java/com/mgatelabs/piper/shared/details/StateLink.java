package com.mgatelabs.piper.shared.details;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Used to join states from a script into a node based system that does not modify the original state
 *
 * Created by @mgatelabs (Michael Fuller) on 10/13/2018 for Phone-Piper.
 */
public class StateLink {

    private final String scriptId;
    private final StateDefinition state;
    private List<StateLink> includes;
    private boolean built;
    private final StateType type;

    /**
     * Used to stop a infinite loop
     */
    private final Set<String> memory;

    public StateLink(final String scriptId, StateDefinition state, Set<String> memory) {
        this.scriptId = scriptId;
        this.state = state;
        if (state.getId().startsWith("@")) {
            type = StateType.FUNCTION;
        } else if (state.getId().startsWith("_")) {
            type = StateType.INCLUDE;
        } else {
            type = StateType.STATE;
        }
        includes = Lists.newArrayList();
        this.memory = Sets.newHashSet(memory);
    }

    public StateDefinition getState() {
        return state;
    }

    public String getScriptId() {
        return scriptId;
    }

    public List<StateLink> getIncludes() {
        return includes;
    }

    public boolean addLink(StateLink link) {
        if (!memory.contains(link.getState().getId())) {
            includes.add(link);
            memory.add(link.getState().getId());
            link.mergeMemory(memory);
            return true;
        }
        return false;
    }

    public boolean isBuilt() {
        return built;
    }

    public void setBuilt(boolean built) {
        this.built = built;
    }

    public StateType getType() {
        return type;
    }

    private void mergeMemory(Set<String> history) {
        this.memory.addAll(history);
    }

    @Override
    public String toString() {
        return scriptId + "." + state.getId();
    }

    public Map<String, VarDefinition> getVariableDefinitions() {
        Map<String, VarDefinition> results = Maps.newHashMap();

        for (VarDefinition varDefinition : state.getVariables().values()) {
            results.put(varDefinition.getName(), varDefinition);
        }

        for (StateLink child : getIncludes()) {
            Map<String, VarDefinition> childResults = child.getVariableDefinitions();
            for (VarDefinition childDefinition : childResults.values()) {
                if (!results.containsKey(childDefinition.getName())) {
                    results.put(childDefinition.getName(), childDefinition);
                }
            }
        }

        return results;
    }

    public Set<String> determineStateIds(final Set<String> exploredStates, final Map<String, ExecutableLink> globalStates) {
        if (exploredStates.contains(state.getId())) return ImmutableSet.of();
        Set<String> screenIds = Sets.newHashSet();
        Set<String> copy = Sets.newHashSet(exploredStates);
        copy.add(state.getId());
        for (StatementDefinition statementDefinition : state.getStatements()) {
            screenIds.addAll(statementDefinition.determineScreenIds(copy, globalStates));
        }
        for (StateLink include : this.getIncludes()) {
            screenIds.addAll(include.determineStateIds(copy, globalStates));
        }
        return ImmutableSet.copyOf(screenIds);
    }
}
