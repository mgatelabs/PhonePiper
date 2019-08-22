package com.mgatelabs.piper.shared.helper;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by mmgat on 9/6/2017.
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
