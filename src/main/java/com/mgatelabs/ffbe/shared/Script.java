package com.mgatelabs.ffbe.shared;

import java.util.*;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 8/28/2017
 */
public class Script {
  private Map<String, ActionDetail> actions;
  private List<SetDetail> sets;

  public Map<String, ActionDetail> getActions() {
    return actions;
  }

  public void setActions(Map<String, ActionDetail> actions) {
    this.actions = actions;
  }

  public List<SetDetail> getSets() {
    return sets;
  }

  public void setSets(List<SetDetail> sets) {
    this.sets = sets;
  }
}
