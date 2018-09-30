package com.mgatelabs.piper.shared.details;

import com.mgatelabs.piper.shared.util.Var;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/23/2017 for Phone-Piper
 */
public enum VarType {
    STRING,
    INT,
    FLOAT;

    public Var cast(Var v) {
        switch (this) {
            case INT: return v.asInt();
            case FLOAT: return v.asFloat();
            case STRING: return v.asString();
        }
        throw new RuntimeException("Unknown VarType");
    }
}
