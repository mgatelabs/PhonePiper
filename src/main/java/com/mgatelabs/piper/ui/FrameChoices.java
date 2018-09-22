package com.mgatelabs.piper.ui;

import com.mgatelabs.piper.shared.details.*;
import com.mgatelabs.piper.shared.mapper.MapDefinition;
import com.mgatelabs.piper.ui.utils.Constants;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 9/20/2017
 */
public class FrameChoices {

    private PlayerDefinition playerDefinition;
    private MapDefinition mapDefinition;
    private ScriptDefinition scriptDefinition;
    private DeviceDefinition deviceDefinition;
    private ViewDefinition viewDefinition;

    private final String deviceName;
    private final String scriptName;
    private final String mapName;
    private final String viewName;

    public enum Action {
        RUN,
        EDIT,
        CREATE,
        DELETE
    }

    public enum Mode {
        SCRIPT,
        MAP,
        DEVICE,
        VIEW
    }

    private final Mode mode;
    private final Action action;

    public FrameChoices(String actionId, String modeId, PlayerDefinition playerDefinition, String mapId, String deviceId, List<String> views, List<String> scripts) {
        this.playerDefinition = playerDefinition;

        switch (modeId) {
            case Constants.MODE_SCRIPT:
                mode = Mode.SCRIPT;
                break;
            case Constants.MODE_MAP:
                mode = Mode.MAP;
                break;
            case Constants.MODE_VIEW:
                mode = Mode.VIEW;
                break;
            case Constants.MODE_DEVICE:
            default:
                mode = Mode.DEVICE;
                break;
        }

        switch (actionId) {
            case Constants.ACTION_CREATE:
                action = Action.CREATE;
                break;
            case Constants.ACTION_EDIT:
                action = Action.EDIT;
                break;
            case Constants.ACTION_DELETE:
                action = Action.DELETE;
                break;
            case Constants.ACTION_RUN:
            default:
                action = Action.RUN;
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
            scriptDefinition = buildScriptDefinition(scripts);
        } else {
            this.scriptDefinition = null;
        }
        this.scriptName = scriptDefinition != null ? scriptDefinition.getScriptId() : "";

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
                    if (action == Action.RUN && viewId2 != null && viewId2.trim().length() > 0) {
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

    private ScriptDefinition buildScriptDefinition(List<String> scripts) {
        ScriptDefinition scriptDef = null;

        for (String scriptId : scripts) {
            if (scriptId.trim().length() > 0) {
                ScriptDefinition otherScriptDef = ScriptDefinition.read(scriptId);

                if (otherScriptDef == null)
                    continue;

                if (scriptDef == null) {
                    scriptDef = otherScriptDef;
                    continue;
                }

                if (action == Action.RUN) {
                    for (Map.Entry<String, StateDefinition> otherState : otherScriptDef.getStates().entrySet()) {
                        scriptDef.getStates().remove(otherState.getKey());
                        scriptDef.getStates().put(otherState.getKey(), otherState.getValue());
                    }
                }
            }
        }
        return scriptDef;
    }

    public boolean isValid() {
        switch (action) {
            case RUN: {
                switch (mode) {
                    case SCRIPT: {
                        return isNotNullString(deviceName) && isNotNullString(scriptName);
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

    public boolean canMap(Action action, Mode mode) {
        if (action == Action.DELETE) return false;
        if (action == Action.CREATE) return false;
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

    public boolean canScript(Action action, Mode mode) {
        if (action == Action.DELETE) return false;
        if (action == Action.CREATE) return false;
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

    public boolean canDevice(Action action, Mode mode) {
        if (action == Action.DELETE) return false;
        if (action == Action.CREATE) return false;
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

    public boolean canView(Action action, Mode mode) {
        if (action == Action.DELETE) return false;
        if (action == Action.CREATE) return false;
        if (mode == Mode.SCRIPT && action == Action.RUN) return true;
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
                        scriptDefinition = new ScriptDefinition(name);
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
        return scriptName;
    }

    public String getMapName() {
        return mapName;
    }

    public String getViewName() {
        return viewName;
    }

    public Mode getMode() {
        return mode;
    }

    public Action getAction() {
        return action;
    }

    public PlayerDefinition getPlayerDefinition() {
        return playerDefinition;
    }

    public MapDefinition getMapDefinition() {
        return mapDefinition;
    }

    public ScriptDefinition getScriptDefinition() {
        return scriptDefinition;
    }

    public DeviceDefinition getDeviceDefinition() {
        return deviceDefinition;
    }

    public ViewDefinition getViewDefinition() {
        return viewDefinition;
    }
}
