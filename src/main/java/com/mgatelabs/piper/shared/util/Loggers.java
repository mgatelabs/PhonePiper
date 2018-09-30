package com.mgatelabs.piper.shared.util;

import com.mgatelabs.piper.Runner;
import com.mgatelabs.piper.ui.utils.WebLogHandler;
import org.apache.juli.FileHandler;

/**
 *
 * Created by @mgatelabs (Michael Fuller) on 9/20/2018
 */
public class Loggers {

    public static FileHandler fileLogger;
    public static WebLogHandler webLogger;

    public static void init() {
        fileLogger = new FileHandler(Runner.WORKING_DIRECTORY.toString(), "piper_", ".log", 3);
        webLogger = new WebLogHandler();
    }
}
