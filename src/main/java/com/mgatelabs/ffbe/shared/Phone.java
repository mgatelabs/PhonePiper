package com.mgatelabs.ffbe.shared;

import java.util.*;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 8/28/2017
 */
public class Phone {
  private Map<String, ScreenDetail> screens;

  public Map<String, ScreenDetail> getScreens() {
    return screens;
  }

  public void setScreens(Map<String, ScreenDetail> screens) {
    this.screens = screens;
  }
}
