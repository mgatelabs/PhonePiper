package com.mgatelabs.ffbe.ui;

import com.mgatelabs.ffbe.shared.details.*;
import com.mgatelabs.ffbe.shared.mapper.MapDefinition;
import com.mgatelabs.ffbe.ui.utils.Constants;

import java.io.File;
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

    public FrameChoices(String actionId, String modeId, PlayerDefinition playerDefinition, String mapId, String scriptId, String scriptId2, String scriptId3, String deviceId, String viewId, String viewId2) {
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
        this.scriptName = scriptId;
        this.deviceName = deviceId;
        this.viewName = viewId;

        if (canMap(action, mode) && mapId != null) {
            this.mapDefinition = new MapDefinition();
        } else {
            this.mapDefinition = null;
        }

        if (canScript(action, mode) && scriptId != null) {
            this.scriptDefinition = ScriptDefinition.read(scriptId);
            if (action == Action.RUN && scriptId2 != null && scriptId2.trim().length() > 0) {
                ScriptDefinition scriptDefinitionOther = ScriptDefinition.read(scriptId2);
                for (Map.Entry<String, StateDefinition> other : scriptDefinitionOther.getStates().entrySet()) {
                    this.scriptDefinition.getStates().remove(other.getKey());
                    this.scriptDefinition.getStates().put(other.getKey(), other.getValue());
                }
            }
            if (action == Action.RUN && scriptId3 != null && scriptId3.trim().length() > 0) {
                ScriptDefinition scriptDefinitionOther = ScriptDefinition.read(scriptId3);
                for (Map.Entry<String, StateDefinition> other : scriptDefinitionOther.getStates().entrySet()) {
                    this.scriptDefinition.getStates().remove(other.getKey());
                    this.scriptDefinition.getStates().put(other.getKey(), other.getValue());
                }
            }
        } else {
            this.scriptDefinition = null;
        }

        if (canDevice(action, mode) && deviceId != null) {
            this.deviceDefinition = DeviceDefinition.read(deviceId);
        } else {
            this.deviceDefinition = null;
        }

        if (canView(action, mode)) {
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
            if (action == Action.RUN && viewId2 != null && viewId2.trim().length() > 0) {
                ViewDefinition otherDefinition = ViewDefinition.read(viewId2);
                ViewDefinition.merge(otherDefinition, viewDefinition, true);
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
