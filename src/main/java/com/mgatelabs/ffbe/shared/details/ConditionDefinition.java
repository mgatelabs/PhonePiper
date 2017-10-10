package com.mgatelabs.ffbe.shared.details;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/3/2017.
 */
public class ConditionDefinition {
    private ConditionType is;
    private String value;
    private String var;

    private List<ConditionDefinition> and;
    private List<ConditionDefinition> or;

    public ConditionType getIs() {
        return is;
    }

    public void setIs(ConditionType is) {
        this.is = is;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void fix() {
        if (and == null) {
            and = Lists.newArrayList();
        } else {
            for (ConditionDefinition conditionDefinition : and) {
                conditionDefinition.fix();
            }
        }
        if (or == null) {
            or = Lists.newArrayList();
        } else {
            for (ConditionDefinition conditionDefinition : or) {
                conditionDefinition.fix();
            }
        }
    }

    public List<ConditionDefinition> getAnd() {
        return and;
    }

    public void setAnd(List<ConditionDefinition> and) {
        this.and = and;
    }

    public List<ConditionDefinition> getOr() {
        return or;
    }

    public void setOr(List<ConditionDefinition> or) {
        this.or = or;
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public Set<String> determineScreenIds() {
        Set<String> screenIds = Sets.newHashSet();

        if (is == ConditionType.SCREEN) {
            screenIds.add(value);
        }

        if (and != null) {
            for (ConditionDefinition conditionDefinition : and) {
                screenIds.addAll(conditionDefinition.determineScreenIds());
            }
        }

        if (or != null) {
            for (ConditionDefinition conditionDefinition : or) {
                screenIds.addAll(conditionDefinition.determineScreenIds());
            }
        }

        return screenIds;
    }
}
