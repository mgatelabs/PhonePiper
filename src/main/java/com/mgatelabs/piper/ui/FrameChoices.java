package com.mgatelabs.piper.ui;

import com.google.common.collect.Lists;
import com.mgatelabs.piper.shared.TreeNode;
import com.mgatelabs.piper.shared.details.*;
import com.mgatelabs.piper.shared.mapper.MapDefinition;
import com.mgatelabs.piper.ui.utils.Constants;

import java.io.File;
import java.util.List;

/**
 *
 * Created by @mgatelabs (Michael Fuller) on 9/20/2017
 */
public class FrameChoices {

    private ScriptEnvironment scriptEnvironment;
    private MapDefinition mapDefinition;
    private DeviceDefinition deviceDefinition;
    private ViewDefinition viewDefinition;

    private final String deviceName;
    private final String mapName;
    private final String viewName;

    private final ScriptEnvironment.Mode mode;
    private final ScriptEnvironment.Action action;

    public FrameChoices(String actionId, String modeId, String mapId, String deviceId, List<String> views, List<String> scripts) {

        switch (modeId) {
            case Constants.MODE_SCRIPT:
                mode = ScriptEnvironment.Mode.SCRIPT;
                break;
            case Constants.MODE_MAP:
                mode = ScriptEnvironment.Mode.MAP;
                break;
            case Constants.MODE_VIEW:
                mode = ScriptEnvironment.Mode.VIEW;
                break;
            case Constants.MODE_DEVICE:
            default:
                mode = ScriptEnvironment.Mode.DEVICE;
                break;
        }

        switch (actionId) {
            case Constants.ACTION_CREATE:
                action = ScriptEnvironment.Action.CREATE;
                break;
            case Constants.ACTION_EDIT:
                action = ScriptEnvironment.Action.EDIT;
                break;
            case Constants.ACTION_DELETE:
                action = ScriptEnvironment.Action.DELETE;
                break;
            case Constants.ACTION_RUN:
            default:
                action = ScriptEnvironment.Action.RUN;
                break;
        }

        this.mapName = mapId;
        this.deviceName = deviceId;
        if (views.size() > 0) {
            this.viewName = views.get(0);
        } else {
            this.viewName = "";
        }

        if (canMap(action, mode) && mapId != null) {
            this.mapDefinition = new MapDefinition();
        } else {
            this.mapDefinition = null;
        }

        if (canScript(action, mode)) {
            ScriptEnvironment.Builder environmentBuilder = ScriptEnvironment.builder()
                    .setAction(action)
                    .setMode(mode)
                    .addScriptIds(scripts);
            scriptEnvironment = environmentBuilder.build();
        } else {
            this.scriptEnvironment = null;
        }

        if (canDevice(action, mode) && deviceId != null) {
            this.deviceDefinition = DeviceDefinition.read(deviceId);
        } else {
            this.deviceDefinition = null;
        }

        if (canView(action, mode)) {
            if (views.size() > 0) {
                final String viewId = views.get(0);
                if (viewId != null) {
                    this.viewDefinition = ViewDefinition.read(viewId);
                } else if (deviceDefinition != null && deviceDefinition.getViewId() != null) {
                    this.viewDefinition = ViewDefinition.read(deviceDefinition.getViewId());
                }
                if (viewDefinition != null && deviceDefinition != null) {
                    deviceDefinition.setViewId(viewId);
                }
                if (viewDefinition != null) {
                    ViewDefinition otherDefinition = ViewDefinition.read("global");
                    if (otherDefinition != null) { // We want to add, but not overwrite
                        ViewDefinition.merge(otherDefinition, viewDefinition, false);
                    }
                }
                for (int i = 1; i < views.size(); i++) {
                    final String viewId2 = views.get(i);
                    if (action == ScriptEnvironment.Action.RUN && viewId2 != null && viewId2.trim().length() > 0) {
                        ViewDefinition otherDefinition = ViewDefinition.read(viewId2);
                        ViewDefinition.merge(otherDefinition, viewDefinition, true);
                    }
                }
            }
            if (viewDefinition != null) {
                viewDefinition.sort();
            }
        } else {
            this.viewDefinition = null;
        }
    }

    public boolean isValid() {
        switch (action) {
            case RUN: {
                switch (mode) {
                    case SCRIPT: {
                        return isNotNullString(deviceName) && scriptEnvironment != null;
                    }
                    default:
                        return false;
                }
            }
            case EDIT: {
                return true;
            }
            case CREATE: {
                return true;
            }
            case DELETE: {
                return true;
            }
        }
        return false;
    }

    public boolean isNotNullString(String s) {
        return s != null && s.trim().length() > 0;
    }

    public boolean canMap(ScriptEnvironment.Action action, ScriptEnvironment.Mode mode) {
        if (action == ScriptEnvironment.Action.DELETE) return false;
        if (action == ScriptEnvironment.Action.CREATE) return false;
        switch (mode) {
            case DEVICE:
            case MAP:
                return true;
            case VIEW:
            case SCRIPT:
                return false;
        }
        return false;
    }

    public boolean canScript(ScriptEnvironment.Action action, ScriptEnvironment.Mode mode) {
        if (action == ScriptEnvironment.Action.DELETE) return false;
        if (action == ScriptEnvironment.Action.CREATE) return false;
        switch (mode) {
            case MAP:
            case VIEW:
                return false;
            case SCRIPT:
            case DEVICE:
                return true;
        }
        return false;
    }

    public boolean canDevice(ScriptEnvironment.Action action, ScriptEnvironment.Mode mode) {
        if (action == ScriptEnvironment.Action.DELETE) return false;
        if (action == ScriptEnvironment.Action.CREATE) return false;
        switch (mode) {
            case VIEW:
                return true;
            case MAP:
            case SCRIPT:
            case DEVICE:
                return true;
        }
        return false;
    }

    public boolean canView(ScriptEnvironment.Action action, ScriptEnvironment.Mode mode) {
        if (action == ScriptEnvironment.Action.DELETE) return false;
        if (action == ScriptEnvironment.Action.CREATE) return false;
        if (mode == ScriptEnvironment.Mode.SCRIPT && action == ScriptEnvironment.Action.RUN) return true;
        switch (mode) {
            case MAP:
            case SCRIPT:
            case DEVICE:
                return false;
            case VIEW: {
                return true;
            }
        }
        return false;
    }

    public boolean canCreate(String name) {
        switch (action) {
            case CREATE: {

                switch (mode) {
                    case DEVICE: {
                        if (DeviceDefinition.exists(name)) {
                            return false;
                        }
                        deviceDefinition = new DeviceDefinition(name);
                        return true;
                    }
                    case MAP: {
                        return false;
                    }
                    case VIEW: {
                        if (ViewDefinition.exists(name)) {
                            return false;
                        }
                        File folder = ViewDefinition.folderPath(name);
                        if (!folder.exists()) {
                            folder.mkdirs();
                        } else {
                            return false;
                        }
                        viewDefinition = new ViewDefinition(name);
                    }
                    break;
                    case SCRIPT: {
                        if (ScriptDefinition.exists(name)) {
                            return false;
                        }
                        scriptEnvironment = ScriptEnvironment.builder().addScriptId(name).build();
                    }
                    break;
                }

            }
            break;
            default:
                return false;
        }

        return true;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getScriptName() {
        return scriptEnvironment.getScriptDefinitions().get(0).getScriptId();
    }

    public String getMapName() {
        return mapName;
    }

    public String getViewName() {
        return viewName;
    }

    public ScriptEnvironment.Mode getMode() {
        return mode;
    }

    public ScriptEnvironment.Action getAction() {
        return action;
    }

    public MapDefinition getMapDefinition() {
        return mapDefinition;
    }

    public ScriptEnvironment getScriptEnvironment() {
        return scriptEnvironment;
    }

    public DeviceDefinition getDeviceDefinition() {
        return deviceDefinition;
    }

    public ViewDefinition getViewDefinition() {
        return viewDefinition;
    }
}
