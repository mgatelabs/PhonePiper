package com.mgatelabs.piper.server.entities;

import com.google.common.collect.Lists;
import com.mgatelabs.piper.shared.details.VarDefinition;

import java.util.List;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 2/14/2018
 */
public class StatusResult {

    public enum Status {
        INIT,
        READY,
        STOPPED,
        STOPPING,
        EDIT_VIEW,
        RUNNING
    }

    private Status status;

    private String state;

    private List<StatusLog> logs;

    private List<VarDefinition> variables;

    public StatusResult() {
        logs = Lists.newArrayList();
        variables = Lists.newArrayList();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<StatusLog> getLogs() {
        return logs;
    }

    public void setLogs(List<StatusLog> logs) {
        this.logs = logs;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<VarDefinition> getVariables() {
        return variables;
    }

    public void setVariables(List<VarDefinition> variables) {
        this.variables = variables;
    }
}
