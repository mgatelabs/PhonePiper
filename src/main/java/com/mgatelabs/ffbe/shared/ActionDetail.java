package com.mgatelabs.ffbe.shared;

import java.util.*;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 8/28/2017
 */
public class ActionDetail {
  private String screen;
  private CommandMode flow;
  private List<CommandDetail> commands;
  private ActionOutcome outcome;
  private int wait;

  public String getScreen() {
    return screen;
  }

  public void setScreen(String screen) {
    this.screen = screen;
  }

  public CommandMode getFlow() {
    return flow;
  }

  public void setFlow(CommandMode flow) {
    this.flow = flow;
  }

  public List<CommandDetail> getCommands() {
    return commands;
  }

  public void setCommands(List<CommandDetail> commands) {
    this.commands = commands;
  }

  public ActionOutcome getOutcome() {
    return outcome;
  }

  public void setOutcome(ActionOutcome outcome) {
    this.outcome = outcome;
  }

  public int getWait() {
    return wait;
  }

  public void setWait(int wait) {
    this.wait = wait;
  }
}
