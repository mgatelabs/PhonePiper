package com.mgatelabs.ffbe.shared.details;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/3/2017.
 */
public enum ActionType {
    TAP, // Screen Tap
    BATCH, // Screen Tap
    SWIPE_UP, // Swipe
    SWIPE_RIGHT, // Swipe
    SWIPE_DOWN, // Swipe
    SWIPE_LEFT, // Swipe
    REPEAT,
    SWAP, // Move to a new state, without taking a new image
    MOVE, // Move to a new state
    PUSH, // Start a new state, but still leave old state alone
    POP, // Don't with this bubble
    WAIT, // Wait a few MS
    MSG, // Show a message
    STOP, // Stop execution
    SET, // Zero a variable
    ADD, // Increment a variable
}
