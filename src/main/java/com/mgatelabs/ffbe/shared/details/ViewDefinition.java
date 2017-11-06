package com.mgatelabs.ffbe.shared.details;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.mgatelabs.ffbe.shared.util.JsonTool;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/1/2017.
 */
public class ViewDefinition {

    @JsonIgnore
    private String viewId;
    private List<ScreenDefinition> screens;
    private List<ComponentDefinition> components;

    public ViewDefinition() {
    }

    public ViewDefinition(String viewId) {
        this.viewId = viewId;
        screens = Lists.newArrayList();
        components = Lists.newArrayList();
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public List<ScreenDefinition> getScreens() {
        return screens;
    }

    public void setScreens(List<ScreenDefinition> screens) {
        this.screens = screens;
    }

    public List<ComponentDefinition> getComponents() {
        return components;
    }

    public void setComponents(List<ComponentDefinition> components) {
        this.components = components;
    }

    public static File folderPath(String viewName) {
        return new File("views/" + viewName + "/");
    }

    public static ViewDefinition read(String viewName) {
        File viewFile = new File("views/" + viewName + "/definition.json");
        if (viewFile.exists()) {
            ObjectMapper objectMapper = JsonTool.INSTANCE;
            try {
                ViewDefinition viewDefinition = objectMapper.readValue(viewFile, ViewDefinition.class);
                viewDefinition.setViewId(viewName);
                return viewDefinition;
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

    public static File getFileFor(String viewName) {
        return new File("views/" + viewName + "/definition.json");
    }

    public static boolean exists(String viewName) {
        return getFileFor(viewName).exists();
    }

    public boolean save() {
        File viewFile =getFileFor(viewId);
        final ObjectMapper objectMapper = JsonTool.INSTANCE;
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

    public void sort() {
        Collections.sort(getScreens(), new Comparator<ScreenDefinition>() {
            @Override
            public int compare(ScreenDefinition o1, ScreenDefinition o2) {
                return o1.getScreenId().compareTo(o2.getScreenId());
            }
        });
        Collections.sort(getComponents(), new Comparator<ComponentDefinition>() {
            @Override
            public int compare(ComponentDefinition o1, ComponentDefinition o2) {
                return o1.getComponentId().compareTo(o2.getComponentId());
            }
        });
    }
}
