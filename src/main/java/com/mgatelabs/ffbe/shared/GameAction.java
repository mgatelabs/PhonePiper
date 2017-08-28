package com.mgatelabs.ffbe.shared;

import com.mgatelabs.ffbe.shared.GameState;

import java.awt.image.BufferedImage;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public abstract class GameAction {

    private final GameState requiredState;
    private final GameState exitState;
    private final String title;
    private final String command;

    private final int waitTime;

    public GameAction(GameState requiredState, GameState exitState, String title, String command, int waitTime) {
        this.requiredState = requiredState;
        this.exitState = exitState;
        this.title = title;
        this.command = command;
        this.waitTime = waitTime * 1000;
    }

    public boolean acceptable() {
        return false;
    }

    // Make sure the screen is ready
    public boolean validate(BufferedImage bufferedImage) {

        return false;
    }

    public GameState getRequiredState() {
        return requiredState;
    }

    public GameState getExitState() {
        return exitState;
    }

    public String getTitle() {
        return title;
    }

    public String getCommand() {
        return command;
    }

    public boolean isMove() {
        return false;
    }

    public boolean isRestart() {
        return false;
    }

    public int getWaitTime() {
        return waitTime;
    }

    /**
     * See if it's finished
     * @return
     */
    public boolean check() {
        return false;
    }

    public abstract SamplePoint[] getPoints();
}
