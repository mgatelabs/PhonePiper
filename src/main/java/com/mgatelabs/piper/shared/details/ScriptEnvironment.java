package com.mgatelabs.piper.shared.details;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mgatelabs.piper.shared.TreeNode;

import java.util.List;
import java.util.Map;

/**
 * @author Sanadis
 * Creation Date: 9/29/2018
 */
public final class ScriptEnvironment {

    public enum Action {
        RUN,
        EDIT,
        CREATE,
        DELETE
    }

    public enum Mode {
        SCRIPT,
        MAP,
        DEVICE,
        VIEW
    }

    private final List<ScriptDefinition> scriptDefinitions;
    private final Action action;
    private final Mode mode;
    private final Map<String, StateDefinition> stateDefinitions;
    private final List<VarDefinition> varDefinitions;
    private final List<VarTierDefinition> varTiers;

    private ScriptEnvironment(List<ScriptDefinition> scriptDefinitions, Action action, Mode mode, Map<String, StateDefinition> stateDefinitions, List<VarDefinition> varDefinitions, List<VarTierDefinition> varTiers) {
        this.scriptDefinitions = ImmutableList.copyOf(scriptDefinitions);
        this.action = action;
        this.mode = mode;
        this.stateDefinitions = stateDefinitions;
        this.varDefinitions = varDefinitions;
        this.varTiers = varTiers;
    }

    public List<ScriptDefinition> getScriptDefinitions() {
        return scriptDefinitions;
    }

    public Action getAction() {
        return action;
    }

    public Mode getMode() {
        return mode;
    }

    public ImmutableMap<String, StateDefinition> getStateDefinitions() {
        return ImmutableMap.copyOf(stateDefinitions);
    }

    public ImmutableList<VarDefinition> getVarDefinitions() {
        return ImmutableList.copyOf(varDefinitions);
    }

    public List<VarTierDefinition> getVarTiers() {
        return ImmutableList.copyOf(varTiers);
    }

    public ImmutableMap<String, StateDefinition> getFilteredStates() {
        Map<String, StateDefinition> tempMap = Maps.newHashMap();
        for (Map.Entry<String, StateDefinition> entry : stateDefinitions.entrySet()) {
            if (!entry.getKey().startsWith("_") || entry.getKey().startsWith("@")) {
                tempMap.put(entry.getKey(), entry.getValue());
            }
        }
        return ImmutableMap.copyOf(tempMap);
    }

    public static ScriptEnvironment.Builder builder() {
        return new ScriptEnvironment.Builder();
    }

    public static final class Builder {
        private List<String> scriptIds = Lists.newArrayList();
        private List<ScriptDefinition> scriptDefinitions = Lists.newArrayList();
        private Map<String, StateDefinition> stateDefinitions = Maps.newLinkedHashMap();
        private Map<String, VarDefinition> varDefinitions = Maps.newLinkedHashMap();
        private Map<String, VarTierDefinition> varTiers = Maps.newLinkedHashMap();
        private Action action;
        private Mode mode;

        public Builder addScriptId(String scriptId) {
            this.scriptIds.add(scriptId);
            return this;
        }

        public Builder addScriptIds(List<String> scriptId) {
            this.scriptIds.addAll(scriptId);
            return this;
        }

        public Builder setAction(Action action) {
            this.action = action;
            return this;
        }

        public Builder setMode(Mode mode) {
            this.mode = mode;
            return this;
        }

        private void buildScriptDefinitions() {
            for (String scriptId : scriptIds) {
                if (scriptId.trim().length() > 0) {
                    scriptDefinitions.add(ScriptDefinition.buildScriptDefinition(scriptId));
                }
            }

            System.out.println("ScriptDefinition tree:");
            for (ScriptDefinition scriptDefinition : scriptDefinitions) {
                System.out.println(scriptDefinition.getScriptDefTree().printNodes());
            }
        }

        private void loadDefinitions(TreeNode<ScriptDefinition> parent) {
            if (action == Action.RUN) {
                ScriptDefinition scriptDef = parent.getData();
                if (scriptDef == null) {
                    return;
                }

                for (TreeNode<ScriptDefinition> scriptDefTreeNode : parent.getChildren()) {
                    loadDefinitions(scriptDefTreeNode);
                }

                this.stateDefinitions.putAll(scriptDef.getStates());

                for (VarDefinition varDefinition : scriptDef.getVars()) {
                    varDefinitions.put(varDefinition.getName(), varDefinition);
                }

                for (VarTierDefinition varTier : scriptDef.getVarTiers()) {
                    varTiers.put(varTier.getId(), varTier);
                }
            }
        }

        private void buildStateDefinitions() {
            for (ScriptDefinition scriptDefinition : scriptDefinitions) {
                for (StateDefinition stateDefinition : scriptDefinition.getStates().values()) {
                    TreeNode<StateDefinition> parentState = new TreeNode<StateDefinition>(stateDefinition);
                    stateDefinition.buildStateDefinitionTree(parentState, ImmutableMap.copyOf(stateDefinitions), 0);
                    stateDefinition.mergeStatementDefinitions();
                }
            }

            System.out.println("StateDefinition tree:");
            for (ScriptDefinition scriptDefinition : scriptDefinitions) {
                for (StateDefinition stateDefinition : scriptDefinition.getStates().values()) {
                    System.out.println(stateDefinition.getStateDefTree().printNodes());
                }
            }
        }

        public ScriptEnvironment build() {
            buildScriptDefinitions();

            for (ScriptDefinition scriptDefinition : scriptDefinitions) {
                loadDefinitions(scriptDefinition.getScriptDefTree());
            }
            buildStateDefinitions();

            return new ScriptEnvironment(scriptDefinitions, action, mode, stateDefinitions, Lists.newArrayList(varDefinitions.values()), Lists.newArrayList(varTiers.values()));
        }
    }
}
