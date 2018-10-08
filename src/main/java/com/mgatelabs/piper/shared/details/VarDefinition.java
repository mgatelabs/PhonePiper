package com.mgatelabs.piper.shared.details;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/23/2017 for Phone-Piper
 */
public class VarDefinition {

    private String name;
    private String description;
    private String tierId;
    private String display;
    private String value;
    private VarType type;
    private VarModify modify;
    private VarDisplay displayType;
    private int order;
    private boolean skipSave;
    private List<NameValuePair> values;

    public VarDefinition() {
        order = 99;
    }

    public VarDefinition(VarDefinition source) {
        this.name = source.name;
        this.description = source.description;
        this.display = source.display;
        this.value = source.value;
        this.type = source.type;
        this.modify = source.modify;
        this.displayType = source.displayType;
        this.order = source.order;
        this.skipSave = source.skipSave;
        this.tierId = StringUtils.isBlank(source.tierId) ? "*" : source.tierId;
        this.values = source.values;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public List<NameValuePair> getValues() {
        return values;
    }

    public void setValues(List<NameValuePair> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return name + " (" + type.name() + ")";
    }
}
