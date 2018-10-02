package com.mgatelabs.piper.server.entities;

import com.google.common.collect.Lists;
import com.mgatelabs.piper.server.StatusEnum;
import com.mgatelabs.piper.shared.details.VarDefinition;
import com.mgatelabs.piper.shared.details.VarTierDefinition;

import java.util.List;

/**
 *
 * Created by @mgatelabs (Michael Fuller) on 2/15/2018
 */
public class PrepResult {

    private StatusEnum status;
    private String webLevel;
    private String fileLevel;

    private List<NamedValueDescriptionItem> states;
    private List<NamedValueItem> components;
    private List<NamedValueItem> screens;
    private List<VarDefinition> variables;
    private List<VarTierDefinition> variableTiers;

    public PrepResult(StatusEnum status) {
        this.status = status;
        states = Lists.newArrayList();
        components = Lists.newArrayList();
        screens = Lists.newArrayList();
        variables = Lists.newArrayList();
        variableTiers = Lists.newArrayList();
    }

    public String getWebLevel() {
        return webLevel;
    }

    public void setWebLevel(String webLevel) {
        this.webLevel = webLevel;
    }

    public String getFileLevel() {
        return fileLevel;
    }

    public void setFileLevel(String fileLevel) {
        this.fileLevel = fileLevel;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public List<NamedValueDescriptionItem> getStates() {
        return states;
    }

    public List<NamedValueItem> getScreens() {
        return screens;
    }

    public List<NamedValueItem> getComponents() {
        return components;
    }

    public List<VarDefinition> getVariables() {
        return variables;
    }

    public List<VarTierDefinition> getVariableTiers() {
        return variableTiers;
    }

    public void setVariableTiers(List<VarTierDefinition> variableTiers) {
        this.variableTiers = variableTiers;
    }
}
