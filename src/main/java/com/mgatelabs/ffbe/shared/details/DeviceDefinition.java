package com.mgatelabs.ffbe.shared.details;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgatelabs.ffbe.shared.util.JsonTool;

import java.io.File;
import java.io.IOException;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/1/2017.
 */
public class DeviceDefinition {
    private String name;
    private String viewId;
    private int width;
    private int height;

    public DeviceDefinition() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public static DeviceDefinition read(String deviceName) {
        File deviceFile = new File("devices/" + deviceName + ".json");
        if (deviceFile.exists()) {
            ObjectMapper objectMapper = JsonTool.INSTANCE;
            try {
                return objectMapper.readValue(deviceFile, DeviceDefinition.class);
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
