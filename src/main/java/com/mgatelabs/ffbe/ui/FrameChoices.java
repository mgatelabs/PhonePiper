package com.mgatelabs.ffbe.ui;

import com.mgatelabs.ffbe.shared.details.DeviceDefinition;
import com.mgatelabs.ffbe.shared.details.PlayerDetail;
import com.mgatelabs.ffbe.shared.details.ScriptDefinition;
import com.mgatelabs.ffbe.shared.details.ViewDefinition;
import com.mgatelabs.ffbe.shared.mapper.MapDefinition;
import com.mgatelabs.ffbe.ui.utils.Constants;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 9/20/2017
 */
public class FrameChoices {

    private final PlayerDetail playerDetail;
    private final MapDefinition mapDefinition;
    private final ScriptDefinition scriptDefinition;
    private final DeviceDefinition deviceDefinition;
    private final ViewDefinition viewDefinition;

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

    public FrameChoices(String actionId, String modeId, PlayerDetail playerDetail, String mapId, String scriptId, String deviceId, String viewId) {
        this.playerDetail = playerDetail;

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
        } else {
            this.scriptDefinition = null;
        }

        if (canDevice(action, mode) && deviceId != null) {
            this.deviceDefinition = DeviceDefinition.read(deviceId);
        } else {
            this.deviceDefinition = null;
        }

        if (canView(action, mode) && viewId != null) {
            this.viewDefinition = ViewDefinition.read(viewId);
        } else {
            this.viewDefinition = null;
        }

    }

    public boolean isValid() {
        switch (action) {
            case RUN: {
                switch (mode) {
                    case SCRIPT: {
                        return true;
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
                return false;
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

    public PlayerDetail getPlayerDetail() {
        return playerDetail;
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
