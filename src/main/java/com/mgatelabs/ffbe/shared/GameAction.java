package com.mgatelabs.ffbe.shared;

import java.awt.image.*;
import java.util.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class GameAction {

  private final GameState requiredState;
  private final GameState exitState;
  private final String title;
  private final ActionOutcome outcome;

  private List<SamplePoint> points;
  private final List<CommandAction> actions;
  private final CommandMode commandMode;

  private final int waitTime;

  public GameAction(GameState requiredState, GameState exitState, ActionOutcome outcome, String title, int waitTime, CommandMode commandMode) {
    this.requiredState = requiredState;
    this.exitState = exitState;
    this.outcome = outcome;
    this.commandMode = commandMode;
    this.actions = new ArrayList<>();
    this.title = title;
    this.waitTime = waitTime * 1000;
    points = null;
  }

  public void addAction(CommandAction action) {
    actions.add(action);
  }

  public boolean acceptable() {
    return false;
  }

  // Make sure the screen is ready
  public boolean validate(BufferedImage bufferedImage) {
    return SamplePoint.validate(getPoints(), bufferedImage);
  }

  public GameState getRequiredState() {
    return requiredState;
  }

  public GameState getExitState() {
    return exitState;
  }

  public String getTitle() {
    return title;
  }

  public boolean isMove() {
    return outcome == ActionOutcome.NEXT;
  }

  public boolean isRestart() {
    return outcome == ActionOutcome.REPEAT;
  }

  public int getWaitTime() {
    return waitTime;
  }

  /**
   * See if it's finished
   *
   * @return
   */
  public boolean check() {
    return false;
  }

  public List<SamplePoint> getPoints() {
    return points;
  }

  public void setPoints(List<SamplePoint> points) {
    this.points = points;
  }

  public List<CommandAction> getActions() {
    return actions;
  }

  public CommandMode getCommandMode() {
    return commandMode;
  }
}
