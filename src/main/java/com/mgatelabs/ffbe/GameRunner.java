package com.mgatelabs.ffbe;

import com.mgatelabs.ffbe.actions.*;
import com.mgatelabs.ffbe.shared.ActionSet;
import com.mgatelabs.ffbe.shared.GameAction;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class GameRunner {

    private static final File OutputImage = new File("output.png");

    public void earthShrine() {

        final List<ActionSet> sets = new ArrayList<>();
        ActionSet set;


        set = new ActionSet("Start Earth Shrine", 2);
        sets.add(set);
        set.addAction(new ConnectionError());
        set.addAction(new MenuFriendRequest());
        set.addAction(new MenuEarthShrineExit());
        // In case it messes up
        set.addAction(new MenuFinish3(true));

        set = new ActionSet("Confirm Goals", 2);
        sets.add(set);
        set.addAction(new ConnectionError());
        set.addAction(new MenuConfirmGoals());

        set = new ActionSet("Confirm Friend", 2);
        sets.add(set);
        set.addAction(new ConnectionError());
        set.addAction(new MenuChooseFriend());

        set = new ActionSet("Depart", 2);
        sets.add(set);
        set.addAction(new ConnectionError());
        set.addAction(new MenuDepart());

        set = new ActionSet("Auto", 1);
        sets.add(set);
        set.addAction(new ConnectionError());
        set.addAction(new GameAuto());

        set = new ActionSet("Finish 1", 8);
        sets.add(set);
        set.addAction(new ConnectionError());
        set.addAction(new MenuFinish1());

        set = new ActionSet("Finish 2", 2);
        sets.add(set);
        set.addAction(new ConnectionError());
        set.addAction(new MenuFinish2());

        set = new ActionSet("Finish 3", 2);
        sets.add(set);
        set.addAction(new ConnectionError());
        set.addAction(new MenuFinish3(false));

        runSets(sets);
    }

    private void runSets(List<ActionSet> sets) {
        int currentSetIndex = 0;
        int maxLoops = 100;

        // Start with a Screen shot

        while (maxLoops > 0) {
            BufferedImage bufferedImage = getScreenshot();
            ActionSet set = sets.get(currentSetIndex);

            int waitTime = set.getWaitTime();

            System.out.println("Running Set: " + set.getTitle());

            for (GameAction action: set.getPossibleActions()) {
                System.out.println("Testing Action: " + action.getTitle());
                if (action.validate(bufferedImage)) {
                    System.out.println("Action Passed");
                    exec(action.getCommand());
                    // Let it override the wait time
                    if (action.getWaitTime() > 0) {
                        waitTime = action.getWaitTime();
                    }
                    if (action.isMove()) {
                        System.out.println("Moving to Next ActionSet");
                        currentSetIndex = (currentSetIndex + 1) % sets.size();
                        if (currentSetIndex == 0) {
                            maxLoops--;
                        }
                        break;
                    } else if (action.isRestart()) {
                        System.out.println("Restarting Action");
                        break; // Stop processing
                    }
                }
            }

            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private BufferedImage getScreenshot() {
        if (exec("./screenshot.bat")) {
            try {
                return readImage();
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private BufferedImage readImage() {
        try {
            BufferedImage bufferedImage = ImageIO.read(OutputImage);
            return bufferedImage;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private boolean exec(final String command) {
        try {
            Process myProcess = Runtime.getRuntime().exec(command);
            myProcess.waitFor();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

}
