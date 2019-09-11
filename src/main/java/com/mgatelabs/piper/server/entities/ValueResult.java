package com.mgatelabs.piper.server.entities;

/**
 * Created by @mgatelabs (Michael Fuller) on 2/14/2018.
 */
public class ValueResult {
    private String value;
    private String status;

    public String getValue() {
        return value;
    }

    public ValueResult setValue(String value) {
        this.value = value;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public ValueResult setStatus(String status) {
        this.status = status;
        return this;
    }
}
