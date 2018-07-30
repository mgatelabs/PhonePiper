package com.mgatelabs.ffbe.server;

import com.mgatelabs.ffbe.shared.details.ConnectionDefinition;
import com.mgatelabs.ffbe.shared.details.DeviceDefinition;
import com.mgatelabs.ffbe.shared.details.ScriptDefinition;
import com.mgatelabs.ffbe.shared.details.ViewDefinition;
import com.mgatelabs.ffbe.shared.helper.DeviceHelper;
import com.mgatelabs.ffbe.shared.mapper.MapDefinition;
import com.mgatelabs.ffbe.shared.util.AdbShell;

/**
 * Created by @mgatelabs (Michael Fuller) on 7/29/2018.
 */
public class EditHolder {

    private ScriptDefinition scriptDefinition;
    private MapDefinition mapDefinition;
    private DeviceDefinition deviceDefinition;
    private ViewDefinition viewDefinition;
    private DeviceHelper deviceHelper;

    private AdbShell shell;

    public EditHolder(ScriptDefinition scriptDefinition, MapDefinition mapDefinition, DeviceDefinition deviceDefinition, ViewDefinition viewDefinition, ConnectionDefinition connectionDefinition, AdbShell shell, DeviceHelper deviceHelper) {
        this.scriptDefinition = scriptDefinition;
        this.mapDefinition = mapDefinition;
        this.deviceDefinition = deviceDefinition;
        this.viewDefinition = viewDefinition;
        this.deviceHelper = deviceHelper;
        this.shell = shell;
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

    public void resetShell() {
        shell = new AdbShell();
    }
}
