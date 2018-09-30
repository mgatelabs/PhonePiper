package com.mgatelabs.piper.shared.details;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/3/2017 for Phone-Piper
 */
public class ActionDefinition {
    private ActionType type;
    private String var;
    private String value;
    private String count;
    private ConditionDefinition condition;

    @JsonProperty("args")
    private Map<String, String> arguments;

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, String> arguments) {
        this.arguments = arguments;
    }

    public ConditionDefinition getCondition() {
        return condition;
    }

    public void setCondition(ConditionDefinition condition) {
        this.condition = condition;
    }

    public void fix() {
        if (arguments == null) {
            arguments = Maps.newHashMap();
        }
        if (condition != null) {
            condition.fix();
        }
    }

    @Override
    public String toString() {
        switch (type) {
            case SWIPE_LEFT: {
                return "Swipe Left: " + value;
            }
            case SWIPE_DOWN: {
                return "Swipe Down: " + value;
            }
            case SLOW_DOWN: {
                return "Slow Down: " + value;
            }
            case SLOW_RIGHT: {
                return "Slow Right: " + value;
            }
            case SLOW_LEFT: {
                return "Slow Left: " + value;
            }
            case SWIPE_UP: {
                return "Swipe Up: " + value;
            }
            case SLOW_UP: {
                return "Slow Up: " + value;
            }
            case SWIPE_RIGHT: {
                return "Swipe Right: " + value;
            }
            case TAP: {
                return "Tap: " + value;
            }
            case ADD: {
                return "Add: " + value + " to Var: " + var;
            }
            case INFO: {
                return "Info Msg: " + value;
            }
            case FINE: {
                return "Fine Msg: " + value;
            }
            case FINEST: {
                return "Finest Msg: " + value;
            }
            case SET: {
                return "Set Var: " + var + " to: " + value;
            }
            case MOVE: {
                return "Move to state: " + value;
            }
            case STOP: {
                return "Stop";
            }
            case WAIT: {
                return "Wait: " + value + "ms";
            }
            case BATCH: {
                return "Batch";
            }
            case REPEAT: {
                return "Repeat";
            }
            case LAP: {
                return "Lap: " + value;
            }
            case MATH: {
                return "Lap: " + value;
            }
            case EVENT:
                return "Event: " + value;
            case INPUT:
                return "Input: " + value;
            case CALL:
                return "Call: " + value;
            case RETURN:
                return "RETURN";
        }
        return "???";
    }

    public Set<String> determineScreenIds(final Set<String> exploredStates, final Map<String, StateDefinition> states) {
        Set<String> found = Sets.newHashSet();

        if (getCondition() != null) {
            found.addAll(getCondition().determineScreenIds(exploredStates, states));
        }

        if (getType() == ActionType.CALL) {
            StateDefinition otherDefinition = states.get(getValue());
            found.addAll(otherDefinition.determineScreenIds(exploredStates, states));
        }

        return found;
    }
}
