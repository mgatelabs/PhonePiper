package com.mgatelabs.piper.shared.details;

import com.mgatelabs.piper.Runner;
import com.mgatelabs.piper.shared.image.SamplePoint;

import java.io.File;
import java.util.List;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/1/2017 for Phone-Piper
 */
public class ScreenDefinition implements Comparable<ScreenDefinition> {

    private String name;

    private String screenId;

    private List<SamplePoint> points;

    private boolean enabled;

    public ScreenDefinition() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getScreenId() {
        return screenId;
    }

    public void setScreenId(String screenId) {
        this.screenId = screenId;
    }

    public List<SamplePoint> getPoints() {
        return points;
    }

    public void setPoints(List<SamplePoint> points) {
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static File getPreviewPath(String viewId, String screenId) {
        return new File(Runner.WORKING_DIRECTORY, "views/" + viewId + "/s-" + screenId + ".png");
    }

    @Override
    public String toString() {
        return (enabled ? "" : "[X] - ") + screenId + " - " + name;
    }

    @Override
    public int compareTo(ScreenDefinition o) {
        return screenId.compareTo(o.name);
    }
}
