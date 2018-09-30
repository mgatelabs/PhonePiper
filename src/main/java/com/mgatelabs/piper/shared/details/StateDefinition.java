package com.mgatelabs.piper.shared.details;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017 for Phone-Piper
 */
public class StateDefinition {
    private String name;
    private String description;
    @JsonIgnore
    private String id;

    private Map<String, VarDefinition> variables;

    private List<StatementDefinition> statements;

    private List<String> includes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<StatementDefinition> getStatements() {
        return statements;
    }

    public void setStatements(List<StatementDefinition> statements) {
        this.statements = statements;
    }

    public Map<String, VarDefinition> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, VarDefinition> variables) {
        this.variables = variables;
    }

    public List<String> determineScreenIds(final Set<String> exploredStates, final Map<String, StateDefinition> states) {
        if (exploredStates.contains(id)) {
            return ImmutableList.of();
        }
        Set<String> screenIds = Sets.newHashSet();
        Set<String> copy = Sets.newHashSet(exploredStates);
        copy.add(id);
        for (StatementDefinition statementDefinition : statements) {
            screenIds.addAll(statementDefinition.determineScreenIds(copy, states));
        }
        return ImmutableList.copyOf(screenIds);
    }

    public List<String> getIncludes() {
        return includes;
    }

    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    public void fix() {
        if (getDescription() == null) {
            setDescription("No description provided");
        }
        if (includes == null) {
            includes = Lists.newArrayList();
        }
        if (statements == null) {
            statements = Lists.newArrayList();
        }
        if (variables == null) {
            variables = Maps.newHashMap();
        }
        // Fix the names
        for (Map.Entry<String, VarDefinition> variableDef : variables.entrySet()) {
            variableDef.getValue().setName(variableDef.getKey());
        }
        for (StatementDefinition statementDefinition : getStatements()) {
            if (statementDefinition.getCondition() != null)
                statementDefinition.getCondition().fix();
            if (statementDefinition.getActions() == null) {
                statementDefinition.setActions(Lists.newArrayList());
            }
            for (ActionDefinition actionDefinition: statementDefinition.getActions()) {
                actionDefinition.fix();
            }
        }
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}
