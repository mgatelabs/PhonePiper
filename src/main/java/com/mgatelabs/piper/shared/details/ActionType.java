package com.mgatelabs.piper.shared.details;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/3/2017.
 */
public enum ActionType {
    TAP(ActionValueType.COMPONENT_ID, false, true, true), // Screen Tap
    BATCH(ActionValueType.START_STOP, false, true, true), // Screen Tap
    SWIPE_UP(ActionValueType.COMPONENT_ID, false, true, true), // Swipe
    SLOW_UP(ActionValueType.COMPONENT_ID, false, true, true), // Swipe
    SWIPE_RIGHT(ActionValueType.COMPONENT_ID, false, true, true), // Swipe
    SWIPE_DOWN(ActionValueType.COMPONENT_ID, false, true, true), // Swipe
    SLOW_DOWN(ActionValueType.COMPONENT_ID, false, true, true), // Swipe
    SLOW_RIGHT(ActionValueType.COMPONENT_ID, false, true, true), // Swipe
    SLOW_LEFT(ActionValueType.COMPONENT_ID, false, true, true), // Swipe
    SWIPE_LEFT(ActionValueType.COMPONENT_ID, false, true, true), // Swipe
    REPEAT(ActionValueType.NONE, false, true, false),
    RETURN(ActionValueType.NONE, false, false, true),
    SWAP(ActionValueType.STATE_ID, false, true, false), // Move to a new state, without taking a new image
    MOVE(ActionValueType.STATE_ID, false, true, false), // Move to a new state
    PUSH(ActionValueType.STATE_ID, false, true, false), // Start a new state, but still leave old state alone
    POP(ActionValueType.STATE_ID, false, true, false), // Don't with this bubble
    WAIT(ActionValueType.INT, false, true, true), // Wait a few MS
    MSG(ActionValueType.STRING, false, true, true), // Show a message
    STOP(ActionValueType.NONE, false, true, true), // Stop execution
    SET(ActionValueType.INT, true, true, true), // Zero a variable
    ADD(ActionValueType.INT, true, true, true), // Increment a variable
    LAP(ActionValueType.ID, false, true, true), // LAP EVENT
    EVENT(ActionValueType.EVENT_ID, false, true, true), // GENERAL EVENT
    CALL(ActionValueType.CALL_ID, false, true, true), // GENERAL EVENT
    INPUT(ActionValueType.INPUT_ID, false, true, true); // GENERIC INPUT

    private final ActionValueType valueType;

    private final boolean varNameRequired;
    private final boolean allowedForState;
    private final boolean allowedForCall;

    ActionType(ActionValueType valueType, boolean varNameRequired, boolean allowedForState, boolean allowedForCall) {
        this.valueType = valueType;
        this.varNameRequired = false;
        this.allowedForCall = allowedForCall;
        this.allowedForState = allowedForState;
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
}
