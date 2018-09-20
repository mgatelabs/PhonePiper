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

    public void setValue(String value) {
        this.value = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
