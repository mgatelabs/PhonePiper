package com.mgatelabs.piper.shared;

import com.mgatelabs.piper.runners.ScriptRunner;

/**
 * Created by @mgatelabs (Michael Fuller) on 10/16/2018 for Phone-Piper
 */
public class ScriptThread extends Thread {
    ScriptRunner runner;
    String state;

    public ScriptThread(ScriptRunner scriptRunner, String state) {
        this.runner = scriptRunner;
        this.state = state;
    }

    @Override
    public void run() {
        super.run();
        this.runner.run(state);
    }
}