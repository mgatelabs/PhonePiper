package com.mgatelabs.piper.shared.details;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/23/2017.
 */
public class VarDefinition {

    private String name;
    private String display;
    private String value;
    private VarType type;
    private VarModify modify;
    private VarDisplay displayType;
    private int order;

    public VarDefinition() {
        order = 99;
    }

    public VarDefinition(String name, String display, String value, VarType type, VarDisplay displayType, VarModify modify, int order) {
        this.name = name;
        this.display = display;
        this.value = value;
        this.type = type;
        this.modify = modify;
        this.displayType = displayType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
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

    public VarDisplay getDisplayType() {
        return displayType;
    }

    public void setDisplayType(VarDisplay displayType) {
        this.displayType = displayType;
    }

    public VarModify getModify() {
        return modify;
    }

    public void setModify(VarModify modify) {
        this.modify = modify;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return name + " (" + type.name() + ")";
    }
}
