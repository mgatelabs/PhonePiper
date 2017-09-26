package com.mgatelabs.ffbe.ui.utils;

import com.google.common.collect.ImmutableList;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/25/2017.
 */
public class CustomHandler extends Handler {

    public BlockingQueue<LogRecord> events = new ArrayBlockingQueue<>(25);

    public CustomHandler() {

    }

    @Override
    public void publish(LogRecord record) {
        //record.ge
        synchronized (events) {
            events.add(record);
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
