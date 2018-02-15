package com.mgatelabs.ffbe.server;

/**
 * Created by @mgatelabs (Michael Fuller) on 2/14/2018.
 */
public class StatusLog {
    private final String source;
    private final String timestamp;
    private final String level;
    private final String message;

    public StatusLog(String source, String timestamp, String level, String message) {
        this.source = source;
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
    }

    public String getSource() {
        return source;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }
}
