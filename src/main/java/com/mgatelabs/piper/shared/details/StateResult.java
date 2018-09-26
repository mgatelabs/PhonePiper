package com.mgatelabs.piper.shared.details;

import com.mgatelabs.piper.shared.util.Var;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017.
 */
public class StateResult {

  private final ActionType type;
  private final ActionDefinition actionDefinition;
  private final StateResult priorStateResult;
  private int actionIndex;
  private final StateDefinition stateDefinition;
  private Var result;

  public static final StateResult REPEAT = new StateResult(ActionType.REPEAT, null, null, 0, null);

  public StateResult(ActionType type, ActionDefinition actionDefinition, StateResult priorStateResult, int actionIndex, StateDefinition stateDefinition) {
    this.type = type;
    this.actionDefinition = actionDefinition;
    this.priorStateResult = priorStateResult;
    this.actionIndex = actionIndex;
    this.stateDefinition = stateDefinition;
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

  public int getActionIndex() {
    return actionIndex;
  }

  public StateDefinition getStateDefinition() {
    return stateDefinition;
  }
}
