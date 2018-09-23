package com.mgatelabs.piper.shared.details;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgatelabs.piper.Runner;
import com.mgatelabs.piper.shared.util.AdbShell;
import com.mgatelabs.piper.shared.util.JsonTool;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.XmlElementDecl;
import java.io.File;
import java.io.IOException;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/20/2017.
 */
public class ConnectionDefinition {

    private String ip;
    private String adb = "adb";
    private String direct;
    private boolean wifi;

    public ConnectionDefinition() {

    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDirect() {
        return direct;
    }

    public void setDirect(String direct) {
        this.direct = direct;
    }

    public boolean isWifi() {
        return wifi;
    }

    public void setWifi(boolean wifi) {
        this.wifi = wifi;
    }

    public String getAdb() {
        return adb;
    }

    public void setAdb(String adb) {
        this.adb = adb;
    }

    public void push() {
        if (StringUtils.isNotBlank(adb)) {
            AdbShell.ADB_PATH = adb;
        } else {
            AdbShell.ADB_PATH = "adb";
        }

        if (StringUtils.isNotBlank(direct)) {
            AdbShell.ADB_DIRECT = direct;
        } else {
            AdbShell.ADB_DIRECT = "";
        }
    }
}
