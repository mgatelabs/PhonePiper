package com.mgatelabs.ffbe.runners;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017.
 */
public class StateResult {
    public enum Type {
        POP,
        PUSH,
        SWAP,
        MOVE,
        REPEAT,
    }

    private final Type type;

    private final String value;

    public static final StateResult POP = new StateResult(Type.POP, "");
    public static final StateResult REPEAT = new StateResult(Type.REPEAT, "");

    public StateResult(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public static StateResult push(String stateName) {
        return new StateResult(Type.PUSH, stateName);
    }

    public static StateResult move(String stateName) {
        return new StateResult(Type.MOVE, stateName);
    }

    public static StateResult swap(String stateName) {
        return new StateResult(Type.SWAP, stateName);
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public static StateResult getPOP() {
        return POP;
    }
}
