package com.mgatelabs.piper.server;

import com.mgatelabs.piper.shared.details.*;
import com.mgatelabs.piper.shared.helper.DeviceHelper;
import com.mgatelabs.piper.shared.mapper.MapDefinition;
import com.mgatelabs.piper.shared.util.AdbShell;

/**
 * Created by @mgatelabs (Michael Fuller) on 7/29/2018.
 */
public class EditHolder {

    private ScriptDefinition scriptDefinition;
    private MapDefinition mapDefinition;
    private DeviceDefinition deviceDefinition;
    private ConnectionDefinition connectionDefinition;
    private ViewDefinition viewDefinition;
    private DeviceHelper deviceHelper;

    private AdbShell shell;

    public EditHolder(ScriptDefinition scriptDefinition, MapDefinition mapDefinition, DeviceDefinition deviceDefinition, ViewDefinition viewDefinition, ConnectionDefinition connectionDefinition, AdbShell shell, DeviceHelper deviceHelper) {
        this.scriptDefinition = scriptDefinition;
        this.mapDefinition = mapDefinition;
        this.deviceDefinition = deviceDefinition;
        this.connectionDefinition = connectionDefinition;
        this.viewDefinition = viewDefinition;
        this.deviceHelper = deviceHelper;
        deviceHelper.setIpAddress(connectionDefinition.getIp());
        this.shell = shell;
    }

    public ScreenDefinition getScreenForId(String id) {
        for (ScreenDefinition screenDefinition: viewDefinition.getScreens()) {
            if (screenDefinition.getScreenId().equals(id)) {
                return screenDefinition;
            }
        }
        return null;
    }

    public ComponentDefinition getComponentForId(String id) {
        for (ComponentDefinition screenDefinition: viewDefinition.getComponents()) {
            if (screenDefinition.getComponentId().equals(id)) {
                return screenDefinition;
            }
        }
        return null;
    }

    public ScriptDefinition getScriptDefinition() {
        return scriptDefinition;
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

    public AdbShell getShell() {
        return shell;
    }

    public void restartShell() {
        shell = new AdbShell(deviceDefinition);
    }

    public ConnectionDefinition getConnectionDefinition() {
        return connectionDefinition;
    }
}
