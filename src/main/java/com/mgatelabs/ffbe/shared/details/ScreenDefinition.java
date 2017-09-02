package com.mgatelabs.ffbe.shared.details;

import com.mgatelabs.ffbe.shared.SamplePoint;

import java.util.List;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/1/2017.
 */
public class ScreenDefinition {

    private String name;

    private String componentId;

    private List<SamplePoint> points;

    public ScreenDefinition() {
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public List<SamplePoint> getPoints() {
        return points;
    }

    public void setPoints(List<SamplePoint> points) {
        this.points = points;
    }
}
