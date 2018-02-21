package com.mgatelabs.ffbe.shared.details;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/23/2017.
 */
public class VarDefinition {

    private String name;
    private String value;
    private VarType type;
    private VarModify modify;

    public VarDefinition() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public VarType getType() {
        return type;
    }

    public void setType(VarType type) {
        this.type = type;
    }

    public VarModify getModify() {
        return modify;
    }

    public void setModify(VarModify modify) {
        this.modify = modify;
    }

    @Override
    public String toString() {
        return name + " (" + type.name() + ")";
    }
}
