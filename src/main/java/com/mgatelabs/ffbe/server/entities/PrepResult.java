package com.mgatelabs.ffbe.server.entities;

import com.google.common.collect.Lists;
import com.mgatelabs.ffbe.server.StatusEnum;
import com.mgatelabs.ffbe.shared.details.VarDefinition;

import java.util.List;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 2/15/2018
 */
public class PrepResult {

    private StatusEnum status;

    private List<NamedValueItem> states;
    private List<NamedValueItem> components;
    private List<VarDefinition> variables;

    public PrepResult(StatusEnum status) {
        this.status = status;
        states = Lists.newArrayList();
        components = Lists.newArrayList();
        variables = Lists.newArrayList();
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public List<NamedValueItem> getStates() {
        return states;
    }

    public List<NamedValueItem> getComponents() {
        return components;
    }

    public List<VarDefinition> getVariables() {
        return variables;
    }
}
