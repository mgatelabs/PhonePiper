package com.mgatelabs.piper.server.entities;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;

/**
 * Created by @mgatelabs (Michael Fuller) on 10/20/2018 for Phone-Piper.
 */
public class ConfigListResponse {
    private String status;
    private String msg;
    private List<LoadRequest> configs;

    public ConfigListResponse(List<LoadRequest> configs) {
        this.status = "ok";
        this.msg = "Loaded";
        this.configs = configs;
    }

    public ConfigListResponse(String msg) {
        this.status = "error";
        this.msg = msg;
        configs = ImmutableList.of();
    }

    public String getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public List<LoadRequest> getConfigs() {
        return configs;
    }
}
