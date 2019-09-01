package com.mgatelabs.piper.server.entities;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by @mgatelabs (Michael Fuller) on 10/20/2018 for Phone-Piper.
 */
public class FileListResult {
    private List<String> views;
    private List<String> scripts;
    private List<String> configs;
    private List<String> states;

    public FileListResult() {
        views = Lists.newArrayList();
        scripts = Lists.newArrayList();
        configs = Lists.newArrayList();
        states = Lists.newArrayList();
    }

    public List<String> getViews() {
        return views;
    }

    public List<String> getScripts() {
        return scripts;
    }

    public List<String> getConfigs() {
        return configs;
    }

    public List<String> getStates() {
        return states;
    }
}
