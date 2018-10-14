package com.mgatelabs.piper.shared.details;

/**
 * Used to keep track of processing
 * Created by @mgatelabs (Michael Fuller) on 10/13/2018 for Phone-Piper.
 */
public class ProcessingStateInfo {
    private int stateIndex;
    private int actionIndex;
    private final StateLink link;

    public ProcessingStateInfo(StateLink link) {
        this.stateIndex = 0;
        this.actionIndex = -1;
        this.link = link;
    }

    public ProcessingStateInfo(ProcessingStateInfo source) {
        this.stateIndex = source.stateIndex;
        this.actionIndex = source.actionIndex;
        this.link = source.link;
    }

    public int getStateIndex() {
        return stateIndex;
    }

    public void setStateIndex(int stateIndex) {
        this.stateIndex = stateIndex;
    }

    public int getActionIndex() {
        return actionIndex;
    }

    public void setActionIndex(int actionIndex) {
        this.actionIndex = actionIndex;
    }

    public StateLink getLink() {
        return link;
    }
}
