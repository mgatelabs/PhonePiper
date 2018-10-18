package com.mgatelabs.piper.shared.details;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mgatelabs.piper.Runner;
import com.mgatelabs.piper.shared.TreeNode;
import com.mgatelabs.piper.shared.util.JsonTool;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017 for Phone-Piper
 */
public class ScriptDefinition {

    @JsonIgnore
    private String scriptId;
    private List<String> imports;
    private List<VarDefinition> vars;
    private List<VarTierDefinition> varTiers;
    private Map<String, StateDefinition> states;

    @SuppressWarnings("unused")
    public ScriptDefinition() {
    }

    public ScriptDefinition(String scriptId) {
        this.scriptId = scriptId;
        vars = Lists.newArrayList();
        states = Maps.newLinkedHashMap();
        imports = Lists.newArrayList();
        varTiers = Lists.newArrayList();
    }

    public void fix() {
        for (StateDefinition stateDefinition : states.values()) {
            stateDefinition.fix();
        }
        for (VarDefinition varDefinition : getVars()) {
            if (varDefinition.getDisplay() == null) {
                varDefinition.setDisplay(varDefinition.getName());
            }
            if (varDefinition.getModify() == null) {
                varDefinition.setModify(VarModify.HIDDEN);
            }
            if (varDefinition.getDisplayType() == null) {
                varDefinition.setDisplayType(VarDisplay.STANDARD);
            }
        }
        if (varTiers == null) {
            varTiers = Lists.newArrayList();
        }
        if (imports == null) {
            imports = Lists.newArrayList();
        }
    }

    public boolean validate() {
        return true;
    }

    public Map<String, StateDefinition> getStates() {
        return states;
    }

    public Map<String, StateDefinition> getFilteredStates() {
        Map<String, StateDefinition> tempMap = Maps.newHashMap();
        for (Map.Entry<String, StateDefinition> entry : states.entrySet()) {
            if (!entry.getKey().startsWith("_") && !entry.getKey().startsWith("@")) {
                tempMap.put(entry.getKey(), entry.getValue());
            }
        }
        return tempMap;
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
        return new File(Runner.WORKING_DIRECTORY, "scripts/" + scriptId + ".json");
    }

    public static boolean exists(String viewName) {
        return getFileFor(viewName).exists();
    }

    public List<String> getImports() {
        return imports;
    }

    public void setImports(List<String> imports) {
        this.imports = imports;
    }

    public ScriptDefinition addImport(String imp) {
        imports.add(imp);
        return this;
    }

    public List<VarTierDefinition> getVarTiers() {
        return varTiers;
    }

    @SuppressWarnings("unused")
    public void setVarTiers(List<VarTierDefinition> varTiers) {
        this.varTiers = varTiers;
    }

    public static ScriptDefinition read(String scriptId) {
        final File deviceFile = getFileFor(scriptId);

        if (deviceFile.exists()) {
            final ObjectMapper objectMapper = JsonTool.getInstance();
            try {
                ScriptDefinition scriptDefinition = objectMapper.readValue(deviceFile, ScriptDefinition.class);

                scriptDefinition.setScriptId(scriptId);

                if (scriptDefinition.getStates() == null) {
                    scriptDefinition.setStates(Maps.newLinkedHashMap());
                } else {
                    for (Map.Entry<String, StateDefinition> entry : scriptDefinition.getStates().entrySet()) {
                        entry.getValue().setId(entry.getKey());
                    }
                }

                if (scriptDefinition.getVars() == null) {
                    scriptDefinition.setVars(Lists.newArrayList());
                }

                scriptDefinition.fix();
                scriptDefinition.validate();

                return scriptDefinition;
            } catch (JsonParseException e) {
                System.out.println(scriptId);
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            } catch (JsonMappingException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }
        return null;
    }

    public boolean save() {
        File scriptFile = getFileFor(scriptId);
        final ObjectMapper objectMapper = JsonTool.getInstance();
        try {
            objectMapper.writeValue(scriptFile, this);
            return true;
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String toString() {
        return getScriptId();
    }
}
