package com.mgatelabs.ffbe.shared.details;

import java.util.List;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017.
 */
public class StateDetail {
    private String name;

    private List<StatementDetail> statements;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<StatementDetail> getStatements() {
        return statements;
    }

    public void setStatements(List<StatementDetail> statements) {
        this.statements = statements;
    }
}
