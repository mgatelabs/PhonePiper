package com.mgatelabs.piper.ui.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.Status;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.spi.LocationAwareLogger;

/**
 * @author Sanadis
 * Creation Date: 10/13/2018
 */
public interface LogAppender {

    Level setLevel(Level level);
    Level getLevel();

    default TurboFilter getDefaultTurboFilter(LoggerContext loggerContext) {
        TurboFilter turboFilter = new TurboFilter() {
            @Override
            public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
                return isLoggable(level) ? FilterReply.ACCEPT : FilterReply.NEUTRAL;
            }
        };
        turboFilter.setContext(loggerContext);
        turboFilter.start();
        return turboFilter;
    }

    default PatternLayoutEncoder getDefaultEncoder(String pattern, LoggerContext loggerContext) {
        PatternLayoutEncoder patternLayout = new PatternLayoutEncoder();
        patternLayout.setPattern(pattern);
        patternLayout.setContext(loggerContext);
        patternLayout.start();
        return patternLayout;
    }

    default boolean isLoggable(Level eventLevel) {
        return eventLevel.isGreaterOrEqual(getLevel());
    }

    default void startAppender(final UnsynchronizedAppenderBase appender) {
        Logger logger = ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(appender.getClass().getName()));
        appender.start();
        logger.info(appender.getName() + " initialized: Started = " + appender.isStarted());
        for (Status status : appender.getStatusManager().getCopyOfStatusList()) {
            String message = "(" + status.getOrigin().getClass().getName() + ") " + status.getMessage();
            switch (status.getLevel()) {
                case Status.ERROR:
                    logger.error(message);
                    break;

                case Status.WARN:
                    logger.warn(message);
                    break;

                default:
                    logger.info(message);
                    break;
            }
        }
    }
}
