package com.mgatelabs.piper.shared.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/7/2017.
 */
public class Closer {
    public static void close(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
