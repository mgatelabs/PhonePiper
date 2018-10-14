package com.mgatelabs.piper.shared.details;

import com.mgatelabs.piper.shared.util.Var;

import java.util.Stack;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017 for Phone-Piper
 */
public class StateResult {

    private final ActionType type;
    private final ActionDefinition actionDefinition;
    private final StateResult priorStateResult;
    private final Stack<ProcessingStateInfo> stack;
    private Var result;

    public static StateResult REPEAT(Stack<ProcessingStateInfo> stack) {
        return new StateResult(ActionType.REPEAT, null, null, stack);
    }

    public static StateResult SOFT_REPEAT(Stack<ProcessingStateInfo> stack) {
        return new StateResult(ActionType.SOFT_REPEAT, null, null, stack);
    }

    public static StateResult RETURN(Stack<ProcessingStateInfo> stack) {
        return new StateResult(ActionType.RETURN, null, null, stack);
    }

    public static StateResult STOP(Stack<ProcessingStateInfo> stack) {
        return new StateResult(ActionType.STOP, null, null, stack);
    }

    public StateResult(ActionType type, ActionDefinition actionDefinition, StateResult priorStateResult, Stack<ProcessingStateInfo> stack) {
        this.type = type;
        this.actionDefinition = actionDefinition;
        this.priorStateResult = priorStateResult;
        this.stack = new Stack<>();
        for (ProcessingStateInfo state : stack) {
            this.stack.push(new ProcessingStateInfo(state));
        }
        this.result = null;
    }

    public Var getResult() {
        return result;
    }

    public void setResult(Var result) {
        this.result = result;
    }

    public ActionType getType() {
        return type;
    }

    public String getValue() {
        return actionDefinition.getValue();
    }

    public ActionDefinition getActionDefinition() {
        return actionDefinition;
    }

    public StateResult getPriorStateResult() {
        return priorStateResult;
    }

    public Stack<ProcessingStateInfo> getStack() {
        return stack;
    }

    @Override
    public String toString() {
        return type.name() + (result != null ? (" [" + result.toString() + "]") : "");
    }
}
