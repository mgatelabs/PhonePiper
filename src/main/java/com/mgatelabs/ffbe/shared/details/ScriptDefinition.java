package com.mgatelabs.ffbe.shared.details;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mgatelabs.ffbe.shared.util.JsonTool;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017.
 */
public class ScriptDefinition {

    @JsonIgnore
    private String scriptId;
    private List<VarDefinition> vars;
    private Map<String, StateDefinition> states;

    @SuppressWarnings("unused")
    public ScriptDefinition() {
    }

    public ScriptDefinition(String scriptId) {
        this.scriptId = scriptId;
        vars = Lists.newArrayList();
        states = Maps.newHashMap();
    }

    public Map<String, StateDefinition> getStates() {
        return states;
    }

    public void setStates(Map<String, StateDefinition> states) {
        this.states = states;
    }

    public List<VarDefinition> getVars() {
        return vars;
    }

    public void setVars(List<VarDefinition> vars) {
        this.vars = vars;
    }

    public String getScriptId() {
        return scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    public static File getFileFor(String scriptId) {
        return new File("scripts/" + scriptId + ".json");
    }

    public static boolean exists(String viewName) {
        return getFileFor(viewName).exists();
    }

    public static ScriptDefinition read(String scriptId) {
        final File deviceFile = getFileFor(scriptId);
        if (deviceFile.exists()) {
            final ObjectMapper objectMapper = JsonTool.INSTANCE;
            try {
                ScriptDefinition scriptDefinition = objectMapper.readValue(deviceFile, ScriptDefinition.class);
                scriptDefinition.setScriptId(scriptId);
                if (scriptDefinition.getStates() == null) {
                    scriptDefinition.setStates(Maps.newHashMap());
                } else {
                    for (Map.Entry<String, StateDefinition> entry : scriptDefinition.getStates().entrySet()) {
                        entry.getValue().setId(entry.getKey());
                    }
                }

                if (scriptDefinition.getVars() == null) {
                    scriptDefinition.setVars(Lists.newArrayList());
                }

                return scriptDefinition;
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
