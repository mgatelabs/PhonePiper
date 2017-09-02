package com.mgatelabs.ffbe.shared.details;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/1/2017.
 */
public class ViewDefinition {

    private List<ScreenDefinition> screens;
    private List<ScreenDefinition> components;

    public ViewDefinition() {
    }

    public List<ScreenDefinition> getScreens() {
        return screens;
    }

    public void setScreens(List<ScreenDefinition> screens) {
        this.screens = screens;
    }

    public List<ScreenDefinition> getComponents() {
        return components;
    }

    public void setComponents(List<ScreenDefinition> components) {
        this.components = components;
    }

    public static ViewDefinition read(String viewName) {
        File viewFile = new File("views/" + viewName + "/definition.json");
        if (viewFile.exists()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(viewFile, ViewDefinition.class);
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

    public boolean save(String viewName) {
        File viewFile = new File("views/" + viewName + "/definition.json");

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(viewFile, this);
            return true;
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
