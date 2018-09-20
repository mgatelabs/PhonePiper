package com.mgatelabs.piper.shared.details;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.mgatelabs.piper.Runner;
import com.mgatelabs.piper.shared.util.JsonTool;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 4/23/2018
 */
public class VarStateDefinition {

    private List<VarDefinition> items;

    public VarStateDefinition() {
        items = Lists.newArrayList();
    }

    public List<VarDefinition> getItems() {
        return items;
    }

    public void addItem(VarDefinition varDefinition) {
        items.add(varDefinition);
    }

    public static File getFileFor(String scriptName) {
        return new File(Runner.WORKING_DIRECTORY,"states/" + scriptName + ".json");
    }

    public static boolean exists(String scriptName) {
        return getFileFor(scriptName).exists();
    }

    public static VarStateDefinition read(String scriptName) {
        File viewFile = getFileFor(scriptName);
        if (viewFile.exists()) {
            ObjectMapper objectMapper = JsonTool.INSTANCE;
            try {
                VarStateDefinition viewDefinition = objectMapper.readValue(viewFile, VarStateDefinition.class);
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

    public boolean save(String scriptName) {
        File viewFile =getFileFor(scriptName);
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

}
