package com.mgatelabs.piper.shared.helper;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/30/2020 for Phone-Piper.
 */
public class ReceiptPrinter {

    private static int index = 1;

    public static synchronized int getNextIndex() {
        index++;
        if (index > 15) {
            index = 1;
        }
        return index;
    }

}
