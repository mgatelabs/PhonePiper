package com.mgatelabs.ffbe;

import com.mgatelabs.ffbe.shared.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class Runner {
    public static void main(String [] args) {
        GameRunner runner = new GameRunner();

        if (args.length == 1 && args[0].equals("snap")) {
            runner.snap();
            return;
        } else if (args.length != 2) {
            System.out.println("Required Parameters: phone script");
            return;
        }

        Phone phone = runner.loadPhone(args[0]);

        Script script = runner.loadScript(args[1]);

        if (phone != null && script != null) {
            runner.run(phone, script);
        }
        //runner.earthShrine();
    }
}
