package com.mgatelabs.ffbe.shared.details;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017.
 */
public class StateDefinition {
    private String name;
    @JsonIgnore
    private String id;

    private List<StatementDefinition> statements;

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

    public List<StatementDefinition> getStatements() {
        return statements;
    }

    public void setStatements(List<StatementDefinition> statements) {
        this.statements = statements;
    }

    public List<String> determineScreenIds() {
        Set<String> screenIds = Sets.newHashSet();
        for (StatementDefinition statementDefinition: statements) {
            screenIds.addAll(statementDefinition.determineScreenIds());
        }
        return ImmutableList.copyOf(screenIds);
    }

    @Override
    public String toString() {
        return id + " - " +  name;
    }
}
