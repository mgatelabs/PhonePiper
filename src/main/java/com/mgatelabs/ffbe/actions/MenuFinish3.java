package com.mgatelabs.ffbe.actions;

import com.mgatelabs.ffbe.shared.GameAction;
import com.mgatelabs.ffbe.shared.GameState;
import com.mgatelabs.ffbe.shared.SamplePoint;

import java.awt.image.BufferedImage;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class MenuFinish3 extends GameAction {

    private boolean restart;

    public MenuFinish3(boolean restart) {
        super(GameState.MENU, GameState.MENU, "Menu Finish 3", "adb shell input tap 720 2250", 5);
        this.restart = restart;
    }

    private static final SamplePoint[] points = {
            // Auto Button
            new SamplePoint(546,329,253,253,253),
            new SamplePoint(546,329,253,253,253),
            new SamplePoint(546,329,253,253,253),
            new SamplePoint(546,329,253,253,253),

            // N
            new SamplePoint(657,2273,255,255,255),
            new SamplePoint(657,2273,255,255,255),
            new SamplePoint(657,2273,255,255,255),
            new SamplePoint(657,2273,255,255,255),

            new SamplePoint(753,2257,255,255,255),
            new SamplePoint(753,2257,255,255,255),
            new SamplePoint(753,2257,255,255,255),
            new SamplePoint(753,2257,255,255,255)


    };

    @Override
    public SamplePoint[] getPoints() {
        return points;
    }

    @Override
    public boolean isMove() {
        return !restart;
    }

    @Override
    public boolean isRestart() {
        return restart;
    }

    @Override
    public boolean validate(BufferedImage bufferedImage) {
        return SamplePoint.validate(points, bufferedImage);
    }

    //

}
