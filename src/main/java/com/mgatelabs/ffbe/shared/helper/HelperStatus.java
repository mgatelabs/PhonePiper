package com.mgatelabs.ffbe.shared.helper;

import java.util.Set;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/6/2017.
 */
public class HelperStatus {

    public enum Status {
        OK,
        FAIL
    }

    private Status status;

    private String msg;

    private String states;

    private int [] pixels;

    private Set<String> screens;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Set<String> getScreens() {
        return screens;
    }

    public void setScreens(Set<String> screens) {
        this.screens = screens;
    }

    public int[] getPixels() {
        return pixels;
    }

    public void setPixels(int[] pixels) {
        this.pixels = pixels;
    }

    public String getStates() {
        return states;
    }

    public void setStates(String states) {
        this.states = states;
    }
}
