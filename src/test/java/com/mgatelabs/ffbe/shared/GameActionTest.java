package com.mgatelabs.ffbe.shared;

import com.mgatelabs.ffbe.actions.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class GameActionTest {
    @org.junit.Test
    public void getPoints() throws Exception {

        List<GameAction> actions = new ArrayList<GameAction>();

        actions.add(new ConnectionError());
        actions.add(new GameAuto());
        actions.add(new MenuChooseFriend());
        actions.add(new MenuConfirmGoals());
        actions.add(new MenuDepart());
        actions.add(new MenuEarthShrineExit());
        actions.add(new MenuFinish1());
        actions.add(new MenuFinish2());
        actions.add(new MenuFinish3(false));
        actions.add(new MenuFriendRequest());

        for (GameAction action: actions) {

            System.out.println("Action: " + action.getTitle());

            for (int i = 0; i < action.getPoints().length; i++) {
                if (i > 0) {
                    System.out.print(',');
                    System.out.print('\n');
                }
                SamplePoint point = action.getPoints()[i];
                System.out.print("{\"x\":" + point.getX() +
                        ", \"y\":" + point.getY() +
                        ", \"r\":" + point.getR() +
                        ", \"g\":" + point.getG() +
                        ", \"b\":" + point.getB() +
                        "}"
                );
            }
            System.out.println();
            System.out.println();
        }

    }
}