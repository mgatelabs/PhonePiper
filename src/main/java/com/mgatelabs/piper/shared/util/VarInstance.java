package com.mgatelabs.piper.shared.util;

import com.mgatelabs.piper.shared.details.VarDefinition;
import com.mgatelabs.piper.shared.details.VarType;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 9/25/2018
 */
public class VarInstance {
    private final String name;
    private final VarType type;
    private Var var;

    public VarInstance(VarDefinition varDefinition) {
        name = varDefinition.getName();
        type = varDefinition.getType();
        switch (varDefinition.getType()) {
            case INT: {
                var = new IntVar(varDefinition.getValue());
            } break;
            case FLOAT: {
                var = new FloatVar(varDefinition.getValue());
            } break;
            case STRING:
            default: {
                var = new StringVar(varDefinition.getValue());
            } break;
        }
    }

    public String getName() {
        return name;
    }

    public VarType getType() {
        return type;
    }

    public Var getVar() {
        return var;
    }

    public void update(Var var) {
        switch (type) {
            case FLOAT: {
                this.var = var.asFloat();
            } break;
            case INT: {
                this.var = var.asInt();
            } break;
            case STRING: {
                this.var = var.asString();
            } break;
        }
    }
}
