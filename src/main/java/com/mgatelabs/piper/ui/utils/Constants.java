package com.mgatelabs.piper.ui.utils;

import java.util.regex.Pattern;

/**
 *
 * Created by @mgatelabs (Michael Fuller) on 9/20/2017
 */
public class Constants {

    public static final String ACTION_RUN = "run";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_CREATE = "create";
    public static final String ACTION_DELETE = "delete";

    public static final String MODE_MAP = "map";
    public static final String MODE_SCRIPT = "script";
    public static final String MODE_DEVICE = "device";
    public static final String MODE_VIEW = "view";

    public static final Pattern ID_PATTERN = Pattern.compile("^[a-zA-Z0-9-_]+$");
}
