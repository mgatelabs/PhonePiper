package com.mgatelabs.ffbe.server;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 2/14/2018
 */
public class StatusResult {

    public enum Status {
        READY,
        STOPPED,
        RUNNING
    }

    private Status status;

    private List<String> log;

    public StatusResult() {
        log = Lists.newArrayList();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<String> getLog() {
        return log;
    }

    public void setLog(List<String> log) {
        this.log = log;
    }
}
