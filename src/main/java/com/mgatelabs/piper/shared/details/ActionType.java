package com.mgatelabs.piper.shared.details;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/3/2017 for Phone-Piper
 */
public enum ActionType {
    TAP(ActionValueType.COMPONENT_ID, false, true, true, false), // Screen Tap
    BATCH(ActionValueType.START_STOP, false, true, true, false), // Screen Tap
    SWIPE_UP(ActionValueType.COMPONENT_ID, false, true, true, false), // Swipe
    SLOW_UP(ActionValueType.COMPONENT_ID, false, true, true, false), // Swipe
    SWIPE_RIGHT(ActionValueType.COMPONENT_ID, false, true, true, false), // Swipe
    SWIPE_DOWN(ActionValueType.COMPONENT_ID, false, true, true, false), // Swipe
    SLOW_DOWN(ActionValueType.COMPONENT_ID, false, true, true, false), // Swipe
    SLOW_RIGHT(ActionValueType.COMPONENT_ID, false, true, true, false), // Swipe
    SLOW_LEFT(ActionValueType.COMPONENT_ID, false, true, true, false), // Swipe
    SWIPE_LEFT(ActionValueType.COMPONENT_ID, false, true, true, false), // Swipe
    COMPONENT(ActionValueType.COMPONENT_ID, false, true, true, true), // Swipe
    PIXEL(ActionValueType.NONE, false, true, true, true), // Swipe
    REPEAT(ActionValueType.NONE, false, true, false, false),
    RETURN(ActionValueType.NONE, false, false, true, true),
    SOFT_REPEAT(ActionValueType.NONE, false, false, false, false),
    MOVE(ActionValueType.STATE_ID, false, true, false, false), // Move to a new state
    WAIT(ActionValueType.INT, false, true, true, false), // Wait a few MS
    INFO(ActionValueType.STRING, false, true, true, true), // Show a info message
    FINER(ActionValueType.STRING, false, true, true, true), // Show a finer message
    FINEST(ActionValueType.STRING, false, true, true, true), // Show a finest message
    STOP(ActionValueType.NONE, false, true, true, true), // Stop execution
    SET(ActionValueType.INT, true, true, true, true), // Zero a variable
    ADD(ActionValueType.INT, true, true, true, true), // Increment a variable
    LAP(ActionValueType.ID, false, true, true, false), // LAP EVENT
    EVENT(ActionValueType.EVENT_ID, false, true, true, false), // GENERAL EVENT
    MATH(ActionValueType.MATH, true, true, true, true), // GENERAL EVENT
    CALL(ActionValueType.CALL_ID, false, true, true, true), // GENERAL EVENT
    RANDOM(ActionValueType.NONE, true, true, true, true), // RANDOM
    INPUT(ActionValueType.INPUT_ID, false, true, true, false); // GENERIC INPUT

    private final ActionValueType valueType;

    private final boolean varNameRequired;
    private final boolean allowedForState;
    private final boolean allowedForCall;
    private final boolean allowedForCondition;

    ActionType(ActionValueType valueType, boolean varNameRequired, boolean allowedForState, boolean allowedForCall, boolean allowedForCondition) {
        this.valueType = valueType;
        this.varNameRequired = varNameRequired;
        this.allowedForCall = allowedForCall;
        this.allowedForState = allowedForState;
        this.allowedForCondition = allowedForCondition;
    }

    public ActionValueType getValueType() {
        return valueType;
    }

    public boolean isVarNameRequired() {
        return varNameRequired;
    }

    public boolean isAllowedForState() {
        return allowedForState;
    }

    public boolean isAllowedForCall() {
        return allowedForCall;
    }

    public boolean isAllowedForCondition() {
        return allowedForCondition;
    }
}
