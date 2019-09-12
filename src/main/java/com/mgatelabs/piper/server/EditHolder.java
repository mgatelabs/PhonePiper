package com.mgatelabs.piper.server;

import com.mgatelabs.piper.shared.details.ComponentDefinition;
import com.mgatelabs.piper.shared.details.ConnectionDefinition;
import com.mgatelabs.piper.shared.details.DeviceDefinition;
import com.mgatelabs.piper.shared.details.ScreenDefinition;
import com.mgatelabs.piper.shared.details.ScriptEnvironment;
import com.mgatelabs.piper.shared.details.ViewDefinition;
import com.mgatelabs.piper.shared.helper.DeviceHelper;
import com.mgatelabs.piper.shared.mapper.MapDefinition;
import com.mgatelabs.piper.shared.util.AdbShell;
import com.mgatelabs.piper.shared.util.AdbWrapper;

/**
 * Created by @mgatelabs (Michael Fuller) on 7/29/2018.
 */
public class EditHolder {

    private ScriptEnvironment scriptEnvironment;
    private MapDefinition mapDefinition;
    private DeviceDefinition deviceDefinition;
    private ConnectionDefinition connectionDefinition;
    private ViewDefinition viewDefinition;
    private DeviceHelper deviceHelper;

    private AdbWrapper shell;

    public EditHolder(ScriptEnvironment scriptEnvironment, MapDefinition mapDefinition, DeviceDefinition deviceDefinition, ViewDefinition viewDefinition, ConnectionDefinition connectionDefinition, AdbWrapper shell, DeviceHelper deviceHelper) {
        this.scriptEnvironment = scriptEnvironment;
        this.mapDefinition = mapDefinition;
        this.deviceDefinition = deviceDefinition;
        this.connectionDefinition = connectionDefinition;
        this.viewDefinition = viewDefinition;
        this.deviceHelper = deviceHelper;
        deviceHelper.setConnectionDefinition(connectionDefinition);
        this.shell = shell;
    }

    public ScreenDefinition getScreenForId(String id) {
        for (ScreenDefinition screenDefinition : viewDefinition.getScreens()) {
            if (screenDefinition.getScreenId().equals(id)) {
                return screenDefinition;
            }
        }
        return null;
    }

    public ComponentDefinition getComponentForId(String id) {
        for (ComponentDefinition screenDefinition : viewDefinition.getComponents()) {
            if (screenDefinition.getComponentId().equals(id)) {
                return screenDefinition;
            }
        }
        return null;
    }

    public ScriptEnvironment getScriptEnvironment() {
        return scriptEnvironment;
    }

    public MapDefinition getMapDefinition() {
        return mapDefinition;
    }

    public DeviceDefinition getDeviceDefinition() {
        return deviceDefinition;
    }

    public ViewDefinition getViewDefinition() {
        return viewDefinition;
    }

    public DeviceHelper getDeviceHelper() {
        return deviceHelper;
    }

    public AdbWrapper getShell() {
        return shell;
    }

    public ConnectionDefinition getConnectionDefinition() {
        return connectionDefinition;
    }
}
