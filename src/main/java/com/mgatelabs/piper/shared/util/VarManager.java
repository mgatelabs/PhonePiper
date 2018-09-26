package com.mgatelabs.piper.shared.util;

import com.google.common.collect.Maps;
import com.mgatelabs.piper.shared.details.StateDefinition;
import com.mgatelabs.piper.shared.details.VarDefinition;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:developer@mgatelabs.com">Michael Fuller</a>
 * Creation Date: 9/25/2018
 */
public class VarManager {

    Map<String, VarInstance> globals;
    Map<String, VarInstance> state;
    Stack<Map<String, VarInstance>> calls;
    Logger logger;

    public VarManager(Logger logger) {
        globals = Maps.newHashMap();
        state = Maps.newHashMap();
        calls = new Stack<>();
    }

    public void global(List<VarDefinition> definitions) {
        globals.clear();
        for (VarDefinition definition: definitions) {
            globals.put(definition.getName(), new VarInstance(definition));
        }
    }

    public void state(StateDefinition stateDefinition, Map<String, String> arguments) {
        state.clear();
        calls.clear();
        // Set with default arguments
        for (Map.Entry<String, VarDefinition> entry: stateDefinition.getVariables().entrySet()) {
            state.put(entry.getKey(), new VarInstance(entry.getValue()));
        }
        // Override with arguments
        for (Map.Entry<String, String> arg: arguments.entrySet()) {
            VarInstance varInstance = state.get(arg.getKey());
            if (varInstance != null) {
                varInstance.update(new StringVar(arg.getValue()));
            } else {
                logger.severe("Argument " + arg.getKey() + " does not related to a state variable");
            }
        }
    }

    // Future use
    public void push(StateDefinition stateDefinition, Map<String, String> arguments) {
        Map<String, VarInstance> callArgs = Maps.newHashMap();
        calls.clear();
        // Set with default arguments
        for (Map.Entry<String, VarDefinition> entry: stateDefinition.getVariables().entrySet()) {
            callArgs.put(entry.getKey(), new VarInstance(entry.getValue()));
        }
        // Override with arguments
        for (Map.Entry<String, String> arg: arguments.entrySet()) {
            VarInstance varInstance = callArgs.get(arg.getKey());
            if (varInstance != null) {
                varInstance.update(new StringVar(arg.getValue()));
            } else {
                logger.severe("Argument " + arg.getKey() + " does not related to a Call variable");
            }
        }
        calls.push(callArgs);
    }

    public void pop() {
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
