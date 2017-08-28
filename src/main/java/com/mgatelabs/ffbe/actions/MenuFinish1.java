package com.mgatelabs.ffbe.actions;

import com.mgatelabs.ffbe.shared.GameAction;
import com.mgatelabs.ffbe.shared.GameState;
import com.mgatelabs.ffbe.shared.SamplePoint;

import java.awt.image.BufferedImage;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class MenuFinish1 extends GameAction {

    public MenuFinish1() {
        super(GameState.MENU, GameState.MENU, "Menu Finish 1", "adb shell input tap 720 2200", 0);
    }

    private static final SamplePoint[] points = {
            // Auto Button
            new SamplePoint(664,2272,255,255,255),
            new SamplePoint(664,2272,255,255,255),
            new SamplePoint(664,2272,255,255,255),
            new SamplePoint(664,2272,255,255,255),

            new SamplePoint(692,2232,255,255,255),
            new SamplePoint(692,2232,255,255,255),
            new SamplePoint(692,2232,255,255,255),
            new SamplePoint(692,2232,255,255,255)
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

    //

}
