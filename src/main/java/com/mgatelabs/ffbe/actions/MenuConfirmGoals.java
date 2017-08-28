package com.mgatelabs.ffbe.actions;

import com.mgatelabs.ffbe.shared.GameAction;
import com.mgatelabs.ffbe.shared.GameState;
import com.mgatelabs.ffbe.shared.SamplePoint;

import java.awt.image.BufferedImage;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class MenuConfirmGoals extends GameAction {
    public MenuConfirmGoals() {
        super(GameState.MENU, GameState.MENU, "Confirm Goals", "adb shell input tap 720 2200", 0);
    }

    private static final SamplePoint[] points = {
            new SamplePoint(664,2234, 255,255,255),
            new SamplePoint(688,2269, 255,255,255),
            new SamplePoint(702,2258, 251,251,251),
            new SamplePoint(744,2255, 250,250,250)
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
