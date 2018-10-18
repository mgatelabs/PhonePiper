package com.mgatelabs.piper.shared.details;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Used to store the environment information
 *
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
    private final Map<String, ExecutableLink> executionMap;
    private final Map<String, VarDefinition> varDefinitions;
    private final Map<String, VarTierDefinition> varTiers;

    private ScriptEnvironment(List<ScriptDefinition> scriptDefinitions, Action action, Mode mode, Map<String, ExecutableLink> executionMap, Map<String, VarDefinition> varDefinitions, Map<String, VarTierDefinition> varTiers) {
        this.scriptDefinitions = ImmutableList.copyOf(scriptDefinitions);
        this.executionMap = ImmutableMap.copyOf(executionMap);
        this.action = action;
        this.mode = mode;
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

    public Map<String, VarDefinition> getVarDefinitions() {
        return ImmutableMap.copyOf(varDefinitions);
    }

    public Map<String, VarTierDefinition> getVarTiers() {
        return ImmutableMap.copyOf(varTiers);
    }

    public ImmutableMap<String, ExecutableLink> getExecutableStates(ImmutableSet<StateType> types) {
        Map<String, ExecutableLink> tempMap = Maps.newHashMap();
        for (Map.Entry<String, ExecutableLink> entry : executionMap.entrySet()) {
            if (types.contains(entry.getValue().getType())) {
                tempMap.put(entry.getKey(), entry.getValue());
            }
        }
        return ImmutableMap.copyOf(tempMap);
    }

    public ExecutableLink getExecutableState(String stateId) {
        return executionMap.get(stateId);
    }

    public static ScriptEnvironment.Builder builder() {
        return new ScriptEnvironment.Builder();
    }

    public static final class Builder {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());
        private List<String> scriptIds = Lists.newArrayList();
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

        /**
         * Build method 2.0
         */
        public ScriptEnvironment build() {
            // Get scripts in reverse order, major scripts, the major includes and then lesser includes
            List<ScriptDefinition> scriptDefinitions = loadOrderedDefinitions(scriptIds, Sets.newHashSet());
            // Build the initial map of links
            Map<String, StateLink> builtLinks = buildInitialStates(scriptDefinitions);
            // Bind every state to another
            bindLinks(builtLinks, filterStateLinks(builtLinks));

            // Create a map of the executable states, these are the only states that can execute in some way
            // Also link every variable to the root, with appropriate overrides
            Map<String, ExecutableLink> executableLinks = Maps.newHashMap();
            for (Map.Entry<String, StateLink> entry : builtLinks.entrySet()) {
                switch (entry.getValue().getType()) {
                    case STATE:
                    case FUNCTION: {
                        executableLinks.put(entry.getKey(), new ExecutableLink(entry.getValue()));
                    }
                }
            }

            // Build global variables and tiers from the highest override first.  Higher values will override lower variables
            Map<String, VarTierDefinition> foundVarTiers = Maps.newHashMap();
            Map<String, VarDefinition> foundVars = Maps.newHashMap();
            for (ScriptDefinition scriptDefinition : scriptDefinitions) {
                // Collect VarTiers
                for (VarTierDefinition varTierDefinition : scriptDefinition.getVarTiers()) {
                    if (!foundVarTiers.containsKey(varTierDefinition.getId())) {
                        foundVarTiers.put(varTierDefinition.getId(), varTierDefinition);
                    }
                }
                // Collect Variables
                for (VarDefinition varDefinition : scriptDefinition.getVars()) {
                    if (!foundVars.containsKey(varDefinition.getName())) {
                        foundVars.put(varDefinition.getName(), varDefinition);
                    }
                }
            }

            // Verify
            Set<ExecutableLink> badLinks = Sets.newHashSet();
            for (ExecutableLink link : executableLinks.values()) {
                try {
                    link.getLink().determineStateIds(Sets.newHashSet(), executableLinks);
                    link.setValid(true);
                } catch (Exception ex) {
                    logger.error(ex.getMessage());
                    link.setValid(false);
                    badLinks.add(link);
                }
            }

            for (ExecutableLink link : badLinks) {
                logger.error("Bad Reference: " + link.getId());
                executableLinks.remove(link.getId());
            }

            return new ScriptEnvironment(scriptDefinitions, action, mode, executableLinks, foundVars, foundVarTiers);
        }

        /**
         * Get all the initial script definitions, in reverse order, scipts that appear earlier and treated better
         */
        private List<ScriptDefinition> loadOrderedDefinitions(List<String> scriptIds, final Set<String> memory) {
            List<ScriptDefinition> found = Lists.newArrayList();
            // Load the scripts in reverse, highest is the biggest override
            for (String scriptId : scriptIds) {
                if (memory.contains(scriptId)) {
                    continue;
                }
                memory.add(scriptId);
                ScriptDefinition scriptDefinition = ScriptDefinition.read(scriptId);
                if (scriptDefinition != null) {
                    scriptDefinition.fix();
                    found.add(0, scriptDefinition);
                }
            }

            List<ScriptDefinition> foundChildren = Lists.newArrayList();
            // Load in the children, the same way, highest is the biggest override
            for (ScriptDefinition scriptDefinition : found) {
                List<ScriptDefinition> children = loadOrderedDefinitions(scriptDefinition.getImports(), memory);
                foundChildren.addAll(children);
            }
            found.addAll(foundChildren);

            return found;
        }

        /**
         * Build the first level links, states higher up override lower states
         */
        private Map<String, StateLink> buildInitialStates(List<ScriptDefinition> scripts) {
            Map<String, StateLink> states = Maps.newHashMap();

            for (ScriptDefinition script : scripts) {
                for (Map.Entry<String, StateDefinition> entry : script.getStates().entrySet()) {
                    if (!states.containsKey(entry.getKey())) {
                        states.put(entry.getKey(), new StateLink(script.getScriptId(), entry.getValue(), Sets.newHashSet()));
                    }
                }
            }
            return states;
        }

        /**
         * Non-recursive link builder
         */
        private void bindLinks(Map<String, StateLink> universe, Set<StateLink> needles) {
            Set<StateLink> future = Sets.newHashSet();
            while (!needles.isEmpty()) {
                for (StateLink link : needles) {
                    for (String includeState : link.getState().getIncludes()) {
                        List<StateLink> links = getLinksFor(includeState, universe);
                        for (StateLink includeLink : links) {
                            if (includeLink != null) {
                                if (link.addLink(includeLink)) {
                                    if (!includeLink.isBuilt()) {
                                        includeLink.setBuilt(true);
                                        future.add(includeLink);
                                    }
                                }
                            }
                        }
                    }
                    link.setBuilt(true);
                }
                needles.clear();
                needles.addAll(future);
                future.clear();
            }
        }

        /**
         * If the link contains a *, allow it to look for any characters in that spot
         */
        private List<StateLink> getLinksFor(String filter, Map<String, StateLink> universe) {
            List<StateLink> links = Lists.newArrayList();

            if (filter.contains("*")) {
                Pattern p = Pattern.compile(filter.replaceAll("\\*", ".*"));
                for (Map.Entry<String, StateLink> entry : universe.entrySet()) {
                    if (p.matcher(entry.getKey()).matches()) {
                        links.add(entry.getValue());
                    }
                }
                // Make sure they are sorted by name
                links.sort(new Comparator<StateLink>() {
                    @Override
                    public int compare(StateLink o1, StateLink o2) {
                        return o1.getState().getId().compareTo(o2.getState().getId());
                    }
                });
            } else {
                StateLink link = universe.get(filter);
                if (link != null) {
                    links.add(link);
                }
            }
            return links;
        }

        /**
         * Quickly return the STATE links
         */
        private Set<StateLink> filterStateLinks(Map<String, StateLink> universe) {
            Set<StateLink> results = Sets.newHashSet();
            for (Map.Entry<String, StateLink> needle : universe.entrySet()) {
                if (needle.getValue().getType() == StateType.STATE)
                    results.add(needle.getValue());
            }
            return results;
        }

    }
}
