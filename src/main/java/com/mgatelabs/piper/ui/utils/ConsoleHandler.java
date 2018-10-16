package com.mgatelabs.piper.ui.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import org.slf4j.LoggerFactory;

/**
 * @author Sanadis
 * Creation Date: 10/15/2018
 */
public class ConsoleHandler extends ConsoleAppender<ILoggingEvent> implements LogAppender {

    private Level logLevel = Level.INFO;

    public ConsoleHandler() {
        setName(this.getClass().getSimpleName());

        LoggerContext loggerContext = ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).getLoggerContext();
        loggerContext.addTurboFilter(getDefaultTurboFilter(loggerContext));

        setContext(loggerContext);
        setEncoder(getDefaultEncoder("[%d{yyyy-MM-dd HH:mm:ss}][%-5level][%-15.15thread][%class{0}.%method:%line] %message %mdc%n", loggerContext));

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