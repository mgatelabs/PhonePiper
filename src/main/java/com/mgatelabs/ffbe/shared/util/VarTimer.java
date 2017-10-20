package com.mgatelabs.ffbe.shared.util;

/**
 * Created by @mgatelabs (Michael Fuller) on 10/20/2017.
 */
public class VarTimer {

    private long start;
    private long last;
    private long end;
    private int count;

    public VarTimer() {
        start = 0;
        end = 0;
        count = -1;
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

    public String toString() {
        if (count < 1) {
            return "Not Ready";
        } else {
            float lastDiff = ((float) (end - last) / 1000000000.0f);
            float avgDiff = ((float) (end - start) / 1000000000.0f) / count;
            return "Time: " + String.format("%2.2f", lastDiff)+ " Average: " + String.format("%2.2f", avgDiff);
        }
    }
}
