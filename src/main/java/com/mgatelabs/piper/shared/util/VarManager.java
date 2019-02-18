package com.mgatelabs.piper.shared.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mgatelabs.piper.shared.details.ExecutableLink;
import com.mgatelabs.piper.shared.details.StateDefinition;
import com.mgatelabs.piper.shared.details.VarDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 *
 * Created by @mgatelabs (Michael Fuller) on 9/25/2018
 */
public class VarManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<String, VarInstance> globals;
    private final Map<String, VarInstance> state;
    private final Stack<Map<String, VarInstance>> calls;

    private String currentSceneId = null;

    public String getCurrentSceneId() {
        return currentSceneId;
    }

    public VarManager() {
        globals = Maps.newHashMap();
        state = Maps.newHashMap();
        calls = new Stack<>();
    }

    public Map<String, VarInstance> GetStateVariables() {
        return ImmutableMap.copyOf(state);
    }

    public void global(List<VarDefinition> definitions) {
        globals.clear();
        for (VarDefinition definition: definitions) {
            globals.put(definition.getName(), new VarInstance(definition));
        }
    }

    public void state(ExecutableLink executableState, Map<String, String> arguments) {
        calls.clear();
        if (!executableState.getId().equalsIgnoreCase(currentSceneId)) {
            // Only reset the state if the state is different
            currentSceneId = executableState.getId();
            state.clear();
            // Set with default arguments
            for (Map.Entry<String, VarDefinition> entry : executableState.getVariables().entrySet()) {
                state.put(entry.getKey(), new VarInstance(entry.getValue()));
            }
            // Override with arguments
            for (Map.Entry<String, String> arg : arguments.entrySet()) {
                VarInstance varInstance = state.get(arg.getKey());
                if (varInstance != null) {
                    varInstance.update(new StringVar(arg.getValue()));
                } else {
                    logger.error("Argument " + arg.getKey() + " does not related to a state variable");
                }
            }
        } else {
            logger.debug("Not updating variables for state, current state is next state");
        }
    }

    // Future use
    public void push(ExecutableLink executableState, Map<String, String> arguments) {
        Map<String, VarInstance> callArgs = Maps.newHashMap();
        // Set with default arguments
        for (Map.Entry<String, VarDefinition> entry: executableState.getVariables().entrySet()) {
            callArgs.put(entry.getKey(), new VarInstance(entry.getValue()));
        }
        // Override with arguments
        for (Map.Entry<String, String> arg: arguments.entrySet()) {
            VarInstance varInstance = callArgs.get(arg.getKey());
            if (varInstance != null) {
                varInstance.update(new StringVar(arg.getValue()));
            } else {
                logger.error("Argument " + arg.getKey() + " does not related to a Call variable");
            }
        }
        calls.push(callArgs);
    }

    public void pop() {
        if (calls.size() == 0) {
            logger.error("Out of Call Pops");
        }
        calls.pop();
    }

    public Var get(String name) {
        VarInstance varInstance = getVarInstance(name);
        if (varInstance != null) {
            return varInstance.getVar();
        }
        return null;
    }

    public void update(String name, Var data) {
        VarInstance varInstance = getVarInstance(name);
        if (varInstance != null) {
            varInstance.update(data);
        }
    }

    public VarInstance getVarInstance(String name) {
        VarInstance v;
        if (!calls.isEmpty()) {
            v = calls.peek().get(name);
            if (v != null) {
                return v;
            }
        }
        if (!state.isEmpty()) {
            v = state.get(name);
            if (v != null) {
                return v;
            }
        }
        return globals.get(name);
    }

}
