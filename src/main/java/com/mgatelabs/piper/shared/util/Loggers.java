package com.mgatelabs.piper.shared.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.status.Status;
import com.mgatelabs.piper.Runner;
import com.mgatelabs.piper.ui.utils.ConsoleHandler;
import com.mgatelabs.piper.ui.utils.FileHandler;
import com.mgatelabs.piper.ui.utils.WebLogHandler;
import org.slf4j.LoggerFactory;

/**
 *
 * Created by @mgatelabs (Michael Fuller) on 9/20/2018
 */
public abstract class Loggers {

    public static ConsoleHandler consoleHandler;
    public static FileHandler fileHandler;
    public static WebLogHandler webHandler;

    public static void init() {
        Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.detachAndStopAllAppenders();

        consoleHandler = new ConsoleHandler();
        logger.addAppender(consoleHandler);

        fileHandler = new FileHandler(Runner.WORKING_DIRECTORY.toString(), "piper_");
        logger.addAppender(fileHandler);

        webHandler = new WebLogHandler();
        logger.addAppender(webHandler);

        // This sets the JUL framework logging level
        logger.setLevel(Level.INFO);

        logger.info("ConsoleHandler initialized: started " + consoleHandler.isStarted());
        for (Status status : consoleHandler.getStatusManager().getCopyOfStatusList()) {
            logger.info(status.getMessage());
        }
        if (consoleHandler.isStarted() && fileHandler.isStarted() && webHandler.isStarted()) {
            logger.info("Loggers initialized.");
        } else {
            logger.warn("Loggers not fully initialized!");
        }
    }
}
