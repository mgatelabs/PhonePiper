package com.mgatelabs.ffbe.actions;

import com.mgatelabs.ffbe.shared.GameAction;
import com.mgatelabs.ffbe.shared.GameState;
import com.mgatelabs.ffbe.shared.SamplePoint;

import java.awt.image.BufferedImage;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class MenuEarthShrineExit extends GameAction {
    public MenuEarthShrineExit() {
        super(GameState.MENU, GameState.MENU, "Select Fire Shrine - Exit", "adb shell input tap 720 1300", 0);
    }

    @Override
    public boolean isMove() {
        return true;
    }

    private static final SamplePoint[] points = {
            // Has Power
            new SamplePoint(148,235,0,133,221),
            // X
            new SamplePoint(88,1231, 243,243,243),
            new SamplePoint(110,1252, 243,243,243),
            new SamplePoint(95,1268, 243,243,243),
            new SamplePoint(134,1230, 243,243,243)
    };

    @Override
    public SamplePoint[] getPoints() {
        return points;
    }

    @Override
    public boolean validate(BufferedImage bufferedImage) {
        return SamplePoint.validate(points, bufferedImage);
    }
}
