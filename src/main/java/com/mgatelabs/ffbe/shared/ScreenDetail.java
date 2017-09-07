package com.mgatelabs.ffbe.shared;

import com.mgatelabs.ffbe.shared.image.SamplePoint;

import java.util.*;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 8/28/2017
 */
public class ScreenDetail {
  private List<SamplePoint> points;
  private Map<String, SwipeDetail> swipes;
  private Map<String, ButtonLocation> buttons;

  public List<SamplePoint> getPoints() {
    return points;
  }

  public void setPoints(List<SamplePoint> points) {
    this.points = points;
  }

  public Map<String, ButtonLocation> getButtons() {
    return buttons;
  }

  public void setButtons(Map<String, ButtonLocation> buttons) {
    this.buttons = buttons;
  }

  public Map<String, SwipeDetail> getSwipes() {
    return swipes;
  }

  public void setSwipes(Map<String, SwipeDetail> swipes) {
    this.swipes = swipes;
  }
}
