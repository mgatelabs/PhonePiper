package com.mgatelabs.piper.shared.details;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/23/2017 for Phone-Piper
 */
public class VarDefinition {

    private String name;
    private String tierId;
    private String display;
    private String value;
    private VarType type;
    private VarModify modify;
    private VarDisplay displayType;
    private int order;
    private boolean skipSave;

    public VarDefinition() {
        order = 99;
    }

    public VarDefinition(final String name, final String display, final String value, final VarType type, final VarDisplay displayType, final VarModify modify, final int order, final boolean skipSave, final String tierId) {
        this.name = name;
        this.display = display;
        this.value = value;
        this.type = type;
        this.modify = modify;
        this.displayType = displayType;
        this.order = order;
        this.skipSave = skipSave;
        this.tierId = StringUtils.isBlank(tierId) ? "*" : tierId;
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
        if (displayType == null) return VarDisplay.STANDARD;
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

    public boolean isSkipSave() {
        return skipSave;
    }

    public void setSkipSave(boolean skipSave) {
        this.skipSave = skipSave;
    }

    public String getTierId() {
        return tierId;
    }

    public void setTierId(String tierId) {
        this.tierId = tierId;
    }

    @Override
    public String toString() {
        return name + " (" + type.name() + ")";
    }
}
