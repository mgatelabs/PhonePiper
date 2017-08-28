package com.mgatelabs.ffbe.shared;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class ActionSet {
    private final List<GameAction> possibleActions;

    private final String title;
    private final int waitTime;

    public ActionSet(String title, int seconds) {
        this.title = title;
        possibleActions = new ArrayList<GameAction>();
        waitTime = seconds * 1000;
    }

    public String getTitle() {
        return title;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void addAction(GameAction action) {
        possibleActions.add(action);
    }

    public List<GameAction> getPossibleActions() {
        return possibleActions;
    }
}
