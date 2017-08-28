package com.mgatelabs.ffbe.actions;

import com.mgatelabs.ffbe.shared.GameAction;
import com.mgatelabs.ffbe.shared.GameState;
import com.mgatelabs.ffbe.shared.SamplePoint;

import java.awt.image.BufferedImage;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class MenuDepart extends GameAction {
    public MenuDepart() {
        super(GameState.MENU, GameState.MENU, "Depart", "adb shell input tap 720 2200", 0);
    }

    private static final SamplePoint[] points = {
            new SamplePoint(603,2229, 255,255,255),
            new SamplePoint(644,2261, 255,255,255),
            new SamplePoint(691,2271, 255,255,255),
            new SamplePoint(756,2255, 255,255,255),
            new SamplePoint(783,2247, 255,255,255),
            new SamplePoint(824,2244, 255,255,255)
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
