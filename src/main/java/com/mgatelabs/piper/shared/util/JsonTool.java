package com.mgatelabs.piper.shared.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 9/21/2017
 */
public class JsonTool {
    public static ObjectMapper INSTANCE = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
}
