package com.mgatelabs.piper.ui.utils;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.common.collect.ImmutableList;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/25/2017 for Phone-Piper
 */
public class WebLogHandler extends AsyncAppender implements LogAppender {

    private final BlockingQueue<ILoggingEvent> events = new ArrayBlockingQueue<>(200);
    private Level logLevel = Level.INFO;

    public WebLogHandler() {
        setName(this.getClass().getSimpleName());
        LoggerContext loggerContext = ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).getLoggerContext();
        loggerContext.addTurboFilter(getDefaultTurboFilter(loggerContext));

        setContext(loggerContext);
        setIncludeCallerData(true);

        addAppender(this);

        startAppender(this);
    }

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        if (isLoggable(iLoggingEvent.getLevel())) {
            if (iLoggingEvent.getMessage().contains("org.apache.catalina.connector")) return;
            super.preprocess(iLoggingEvent);
            synchronized (events) {
                while (events.size() >= 200) {
                    events.poll();
                }
                events.add(iLoggingEvent);
            }
        }
    }

    public ImmutableList<ILoggingEvent> getEvents() {
        synchronized (events) {
            ImmutableList<ILoggingEvent> temp = ImmutableList.copyOf(events);
            events.clear();
            return temp;
        }
    }

    @Override
    public Level getLevel() {
        return logLevel;
    }

    @Override
    public Level setLevel(Level logLevel) {
//        for (Filter filter : getCopyOfAttachedFiltersList()) {
//            if (filter instanceof ThresholdFilter) {
//                ((ThresholdFilter) filter).setLevel(logLevel.toString());
//            }
//        }
        Level priorLevel = getLevel();
        this.logLevel = logLevel;
        return priorLevel;
    }
}
