package com.mgatelabs.piper.shared.helper;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/30/2020 for Phone-Piper.
 */
public class RefreshReceipt {

    private final int index;
    private final long timestamp;
    private final boolean success;

    public RefreshReceipt(int index, long timestamp, boolean success) {
        this.index = index;
        this.timestamp = timestamp;
        this.success = success;
    }

    public int getIndex() {
        return index;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isSuccess() {
        return success;
    }
}
