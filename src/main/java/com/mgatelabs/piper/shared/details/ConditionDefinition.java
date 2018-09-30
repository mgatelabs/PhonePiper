package com.mgatelabs.piper.shared.details;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/3/2017 for Phone-Piper
 */
public class ConditionDefinition {
    private ConditionType is;
    private ConditionType not;
    private String value;
    private String var;

    @JsonProperty("args")
    private Map<String, String> arguments;

    private List<ConditionDefinition> and;
    private List<ConditionDefinition> andOr;
    private List<ConditionDefinition> or;

    public ConditionType getIs() {
        return is;
    }

    public void setIs(ConditionType is) {
        this.is = is;
    }

    public ConditionType getNot() {
        return not;
    }

    public void setNot(ConditionType not) {
        this.not = not;
    }

    @JsonIgnore
    public ConditionType getUsedCondition() {
        if (is != null && not == null) {
            return is;
        } else if (is == null && not != null) {
            return not;
        } else if (is == null && not == null) {
            throw new RuntimeException("Condition is missing a is or not condition");
        } else {
            throw new RuntimeException("Condition has a is and not condition, you can only use one, not both");
        }
    }

    @JsonIgnore
    public boolean isReversed() {
        return not != null;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, String> arguments) {
        this.arguments = arguments;
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
        if (andOr == null) {
            andOr = Lists.newArrayList();
        } else {
            for (ConditionDefinition conditionDefinition : andOr) {
                conditionDefinition.fix();
            }
        }
        if (arguments == null) {
            arguments = ImmutableMap.of();
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

    public List<ConditionDefinition> getAndOr() {
        return andOr;
    }

    public void setAndOr(List<ConditionDefinition> andOr) {
        this.andOr = andOr;
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public Set<String> determineScreenIds(final Set<String> exploredStates, final Map<String, StateDefinition> states) {
        Set<String> screenIds = Sets.newHashSet();

        if (getUsedCondition() == ConditionType.CALL) {
            StateDefinition stateDefinition = states.get(value);
            screenIds.addAll(stateDefinition.determineScreenIds(exploredStates, states));
        } else if (getUsedCondition() == ConditionType.SCREEN) {
            screenIds.add(value);
        }

        if (and != null) {
            for (ConditionDefinition conditionDefinition : and) {
                screenIds.addAll(conditionDefinition.determineScreenIds(exploredStates, states));
            }
        }

        if (or != null) {
            for (ConditionDefinition conditionDefinition : or) {
                screenIds.addAll(conditionDefinition.determineScreenIds(exploredStates, states));
            }
        }

        if (andOr != null) {
            for (ConditionDefinition conditionDefinition : andOr) {
                screenIds.addAll(conditionDefinition.determineScreenIds(exploredStates, states));
            }
        }

        return screenIds;
    }

    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();

        if (!or.isEmpty()) {
            stringBuilder.append("( ");
        }

        if (!and.isEmpty()) {
            stringBuilder.append("( ");
        }

        stringBuilder.append(getConditionString(this));

        if (!and.isEmpty()) {
            for (ConditionDefinition subCondition : and) {
                stringBuilder.append(" && ");
                stringBuilder.append(subCondition.toString());
            }
            stringBuilder.append(" )");
        }

        if (!or.isEmpty()) {
            for (ConditionDefinition subCondition : or) {
                stringBuilder.append(" || ");
                stringBuilder.append(subCondition.toString());
            }
            stringBuilder.append(" )");
        }

        return stringBuilder.toString();
    }

    public static String getConditionString(ConditionDefinition definition) {
        StringBuilder stringBuilder = new StringBuilder();

        if (definition == null) return "NULL";

        ConditionType conditionType = definition.getUsedCondition();


        switch (conditionType) {
            case LESS: {
                stringBuilder.append(definition.getNot() != null ? "!" : "").append(definition.getVar()).append(definition.getNot() != null ? " >= " : " < ").append(definition.getValue());
            }
            break;

            case EQUAL: {
                stringBuilder.append(definition.getVar()).append(definition.getNot() != null ? " != " : " == ").append(definition.getValue());
            }
            break;

            case SCREEN: {
                stringBuilder.append(definition.getNot() != null ? "!" : "").append("hasScreen('").append(definition.getValue()).append("')");
            }
            break;

            case GREATER: {
                stringBuilder.append(definition.getVar()).append(definition.getNot() != null ? " <= " : " > ").append(definition.getValue());
            }
            break;
            case CALL: {
                stringBuilder.append(definition.getNot() != null ? "!" : "").append("function('").append(definition.getValue()).append("')");
            }
            case BOOLEAN: {
                final boolean booleanValue = "true".equalsIgnoreCase(definition.getValue());
                stringBuilder.append(definition.getNot() != null ? !booleanValue : booleanValue);
            }
            break;
        }

        return stringBuilder.toString();
    }
}
