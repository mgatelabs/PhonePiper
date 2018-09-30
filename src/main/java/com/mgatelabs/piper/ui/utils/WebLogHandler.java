package com.mgatelabs.piper.ui.utils;

import com.google.common.collect.ImmutableList;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/25/2017 for Phone-Piper
 */
public class WebLogHandler extends Handler {

    public BlockingQueue<LogRecord> events = new ArrayBlockingQueue<>(200);

    public WebLogHandler() {

    }

    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            //record.ge
            synchronized (events) {
                while (events.size() >= 200) {
                    events.poll();
                }
                events.add(record);
            }
        }
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }

    public ImmutableList<LogRecord> getEvents() {
        synchronized (events) {
            ImmutableList<LogRecord> temp = ImmutableList.copyOf(events);
            events.clear();
            return temp;
        }
    }
}
