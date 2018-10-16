package com.mgatelabs.piper.ui.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author Sanadis
 * Creation Date: 10/13/2018
 */
public class FileHandler extends RollingFileAppender<ILoggingEvent> implements LogAppender {

    private Level logLevel = Level.INFO;

    public FileHandler(String filePath, String fileName) {
        setName(this.getClass().getSimpleName());
        LoggerContext loggerContext = ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).getLoggerContext();
        loggerContext.addTurboFilter(getDefaultTurboFilter(loggerContext));

        setName(this.getClass().getSimpleName());
        setContext(loggerContext);;
        setEncoder(getDefaultEncoder("[%d{yyyy-MM-dd HH:mm:ss}][%level][%thread][%class.%method:%line] %message %mdc%n", loggerContext));

        TimeBasedRollingPolicy policy = new TimeBasedRollingPolicy<>();
        policy.setFileNamePattern(filePath + File.separator + fileName + "%d{yyyy-MM-dd}.log");
        policy.setMaxHistory(3);
        policy.setContext(loggerContext);
        policy.setParent(this);
        policy.start();
        setRollingPolicy(policy);

        startAppender(this);
    }

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        if (isLoggable(iLoggingEvent.getLevel())) {
            super.append(iLoggingEvent);
        }
    }

    @Override
    public Level getLevel() {
        return logLevel;
    }

    @Override
    public Level setLevel(Level logLevel) {
        Level priorLevel = getLevel();
        this.logLevel = logLevel;
        return priorLevel;
    }
}
