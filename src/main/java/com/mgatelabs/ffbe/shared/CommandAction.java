package com.mgatelabs.ffbe.shared;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 8/28/2017
 */
public class CommandAction {
  private final ActionType type;
  private final int x;
  private final int y;

  public CommandAction(ActionType type, int x, int y) {
    this.type = type;
    this.x = x;
    this.y = y;
  }

  public ActionType getType() {
    return type;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }
}
