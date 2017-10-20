package com.mgatelabs.ffbe.shared.details;

import java.io.File;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/1/2017.
 */
public class ComponentDefinition {

    private String name;

    private String componentId;

    private int x;
    private int y;
    private int w;
    private int h;

    private boolean enabled;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static File getPreviewPath(String viewId, String componentId) {
        return new File("views/" + viewId + "/c-" + componentId + ".png");
    }

    @Override
    public String toString() {
        return (enabled ? "" : "[X] - ") + componentId + " - " + name;
    }
}
