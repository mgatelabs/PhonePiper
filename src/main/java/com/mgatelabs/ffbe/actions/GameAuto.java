package com.mgatelabs.ffbe.actions;

import com.mgatelabs.ffbe.shared.GameAction;
import com.mgatelabs.ffbe.shared.GameState;
import com.mgatelabs.ffbe.shared.SamplePoint;

import java.awt.image.BufferedImage;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class GameAuto extends GameAction {
    public GameAuto() {
        super(GameState.MENU, GameState.MENU, "Auto Attack", "adb shell input tap 160 2450", 30);
    }

    private static final SamplePoint[] points = {
            // Auto Button
            new SamplePoint(104, 2479, 255, 255, 255),
            new SamplePoint(104, 2479, 255, 255, 255),
            new SamplePoint(104, 2479, 255, 255, 255),
            new SamplePoint(104, 2479, 255, 255, 255),

            // Menu Button
            new SamplePoint(1145, 2447, 255, 255, 255),
            new SamplePoint(1145, 2447, 255, 255, 255),
            new SamplePoint(1145, 2447, 255, 255, 255),
            new SamplePoint(1145, 2447, 255, 255, 255)

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
