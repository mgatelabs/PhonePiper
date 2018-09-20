package com.mgatelabs.piper.shared.util;

/**
 * Created by @mgatelabs (Michael Fuller) on 10/20/2017.
 */
public class VarTimer {

    private long start;
    private long last;
    private long end;
    private int count;
    private boolean lapBased;

    public VarTimer(boolean lapBased) {
        start = 0;
        end = 0;
        count = -1;
        this.lapBased = lapBased;
        if (!lapBased) {
            start = System.nanoTime();
        }
    }

    public void time() {
        if (count == -1) {
            start = System.nanoTime();
            end = start;
        } else {
            last = end;
            end = System.nanoTime();
        }
        count++;
    }

    public void reset() {
        start = System.nanoTime();
        end = start;
    }

    public void forward() {
        end = System.nanoTime();
    }

    public long getElapsed() {
        return end - start;
    }

    public String toString() {
        if (count < 1 && lapBased) {
            return "Not Ready";
        } else {
            float lastDiff = ((float) (end - last) / 1000000000.0f);
            float avgDiff = ((float) (end - start) / 1000000000.0f) / count;
            return "Time: " + TimeTo.format(lastDiff)+ " Average: " + TimeTo.format(avgDiff);
        }
    }
}
