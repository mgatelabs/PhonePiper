package com.mgatelabs.piper.server.entities;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by @mgatelabs (Michael Fuller) on 10/20/2018 for Phone-Piper.
 */
public class FileListResult {
    private List<String> views;
    private List<String> scripts;

    public FileListResult() {
        views = Lists.newArrayList();
        scripts = Lists.newArrayList();
    }

    public List<String> getViews() {
        return views;
    }

    public List<String> getScripts() {
        return scripts;
    }
}
