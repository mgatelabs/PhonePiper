package com.mgatelabs.piper;

import com.mgatelabs.piper.server.ServerRunner;
import com.mgatelabs.piper.shared.util.Closer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.io.BufferedInputStream;
import java.io.File;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017 for Phone-Piper
 */
public class Runner {

    public static final String VERSION;

    public static File WORKING_DIRECTORY = new File(".");
    public static String ADB_NAME = "adb";

    static {
        BufferedInputStream bui = new BufferedInputStream(Runner.class.getClassLoader().getResourceAsStream("version.txt"));
        StringBuilder sb = new StringBuilder();
        try {
            int c;
            while ((c = bui.read()) != -1) {
                sb.append((char) c);
            }
        } catch (Exception ex) {
            sb.append("??");
        } finally {
            Closer.close(bui);
        }
        VERSION = sb.toString();
    }

    public static void handleStaticArgs(final String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if (StringUtils.equalsIgnoreCase(args[i], "-working")) {
                WORKING_DIRECTORY = new File(args[i + 1]);
                break;
            }
            if (StringUtils.equalsIgnoreCase(args[i], "-adb")) {
                ADB_NAME = args[i + 1];
                break;
            }
        }
    }

    public static void main(final String[] args) {
        handleStaticArgs(args);
        System.out.println("================================================================================");
        System.out.println("Phone Piper");
        System.out.println("================================================================================");
        System.out.println("Working Directory: " + WORKING_DIRECTORY.getAbsolutePath());
        System.out.println("================================================================================");
        // Just run the server and let it handle everything else
        new SpringApplicationBuilder(ServerRunner.class).headless(false).run(args);
    }
}
