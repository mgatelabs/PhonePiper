package com.mgatelabs.ffbe.actions;

import com.mgatelabs.ffbe.shared.GameAction;
import com.mgatelabs.ffbe.shared.GameState;
import com.mgatelabs.ffbe.shared.SamplePoint;

import java.awt.image.BufferedImage;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class MenuFinish2 extends GameAction {

    public MenuFinish2() {
        super(GameState.MENU, GameState.MENU, "Menu Finish 2", "adb shell input tap 720 2200", 5);
    }

    private static final SamplePoint[] points = {
            // Auto Button
            new SamplePoint(449,348,193,209,235),
            new SamplePoint(449,348,193,209,235),
            new SamplePoint(449,348,193,209,235),
            new SamplePoint(449,348,193,209,235),

            new SamplePoint(585,323,193,209,235),
            new SamplePoint(585,323,193,209,235),
            new SamplePoint(585,323,193,209,235),
            new SamplePoint(585,323,193,209,235)

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
