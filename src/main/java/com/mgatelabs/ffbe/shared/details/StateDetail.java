package com.mgatelabs.ffbe.shared.details;

import java.util.List;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017.
 */
public class StateDetail {
    private String name;

    private List<StatementDefinition> statements;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<StatementDefinition> getStatements() {
        return statements;
    }

    public void setStatements(List<StatementDefinition> statements) {
        this.statements = statements;
    }
}
