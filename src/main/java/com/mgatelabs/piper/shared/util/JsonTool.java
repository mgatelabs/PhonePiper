package com.mgatelabs.piper.shared.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 *
 * Created by @mgatelabs (Michael Fuller) on 9/21/2017
 */
public class JsonTool {
    private static ObjectMapper INSTANCE = null;

    private JsonTool() {
        INSTANCE = new ObjectMapper();
        INSTANCE.enable(SerializationFeature.INDENT_OUTPUT);
        INSTANCE.enable(JsonParser.Feature.ALLOW_COMMENTS);
    }

    public static ObjectMapper getInstance() {
        if (INSTANCE == null)
            new JsonTool();

        return INSTANCE;
    }
}
