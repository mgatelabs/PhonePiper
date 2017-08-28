package com.mgatelabs.ffbe.actions;

import com.mgatelabs.ffbe.shared.GameAction;
import com.mgatelabs.ffbe.shared.GameState;
import com.mgatelabs.ffbe.shared.SamplePoint;

import java.awt.image.BufferedImage;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class MenuChooseFriend extends GameAction {
    public MenuChooseFriend() {
        super(GameState.MENU, GameState.MENU, "Confirm Goals", "adb shell input tap 720 938", 0);
    }

    private static final SamplePoint[] points = {
            new SamplePoint(881,730, 255,255,255),
            new SamplePoint(914,748, 255,255,255),
            new SamplePoint(944,757, 255,255,255),
            new SamplePoint(977,744, 255,255,255)
    };

    @Override
    public SamplePoint[] getPoints() {
        return points;
    }

    @Override
    public boolean isMove() {
        return true;
    }

    @Override
    public boolean validate(BufferedImage bufferedImage) {
        return SamplePoint.validate(points, bufferedImage);
    }
}
