package com.mgatelabs.ffbe.shared.details;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017.
 */
public class ScriptDetail {

    Map<String, StateDetail> states;

    public Map<String, StateDetail> getStates() {
        return states;
    }

    public void setStates(Map<String, StateDetail> states) {
        this.states = states;
    }

    public static ScriptDetail read(String deviceName) {
        File deviceFile = new File("scripts/" + deviceName + ".json");
        if (deviceFile.exists()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(deviceFile, ScriptDetail.class);
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
}
