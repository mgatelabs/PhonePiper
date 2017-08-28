package com.mgatelabs.ffbe.actions;

import com.mgatelabs.ffbe.shared.GameAction;
import com.mgatelabs.ffbe.shared.GameState;
import com.mgatelabs.ffbe.shared.SamplePoint;

import java.awt.image.BufferedImage;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class ConnectionError extends GameAction {

    public ConnectionError() {
        super(GameState.MENU, GameState.MENU, "Connection Error", "adb shell input tap 720 1480", 0);
    }

    private static final SamplePoint[] points = {
            // Blackness
            /*
            new SamplePoint(150,902,0,0,0),
            new SamplePoint(150,902,0,0,0),
            new SamplePoint(150,902,0,0,0),
            new SamplePoint(150,902,0,0,0),

            new SamplePoint(154,278,0,0,0),
            new SamplePoint(154,278,0,0,0),
            new SamplePoint(154,278,0,0,0),
            new SamplePoint(154,278,0,0,0),

            new SamplePoint(824,260,0,0,0),
            new SamplePoint(824,260,0,0,0),
            new SamplePoint(824,260,0,0,0),
            new SamplePoint(824,260,0,0,0),

            new SamplePoint(172,1850,0,0,0),
            new SamplePoint(172,1850,0,0,0),
            new SamplePoint(172,1850,0,0,0),
            new SamplePoint(172,1850,0,0,0),

            new SamplePoint(944,1820,0,0,0),
            new SamplePoint(944,1820,0,0,0),
            new SamplePoint(944,1820,0,0,0),
            new SamplePoint(944,1820,0,0,0),
            */


            // O
            new SamplePoint(671,1480,255,255,255),
            new SamplePoint(671,1480,255,255,255),
            new SamplePoint(671,1480,255,255,255),
            new SamplePoint(671,1480,255,255,255),

            new SamplePoint(676,1462,255,255,255),
            new SamplePoint(676,1462,255,255,255),
            new SamplePoint(676,1462,255,255,255),
            new SamplePoint(676,1462,255,255,255),

            // K
            new SamplePoint(735,1458,255,255,255),
            new SamplePoint(735,1458,255,255,255),
            new SamplePoint(735,1458,255,255,255),
            new SamplePoint(735,1458,255,255,255),

            new SamplePoint(742,1480,255,255,255),
            new SamplePoint(742,1480,255,255,255),
            new SamplePoint(742,1480,255,255,255),
            new SamplePoint(742,1480,255,255,255),

            new SamplePoint(749,1482,255,255,255),
            new SamplePoint(749,1482,255,255,255),
            new SamplePoint(749,1482,255,255,255),
            new SamplePoint(749,1482,255,255,255),

            // Border
            new SamplePoint(716,1558,253,253,253),
            new SamplePoint(716,1558,253,253,253),
            new SamplePoint(716,1558,253,253,253),
            new SamplePoint(716,1558,253,253,253),

            new SamplePoint(546,1398,243,243,243),
            new SamplePoint(546,1398,243,243,243),
            new SamplePoint(546,1398,243,243,243),
            new SamplePoint(546,1398,243,243,243),

            // Blueness

            new SamplePoint(230,1526,0,15,52),
            new SamplePoint(230,1526,0,15,52),
            new SamplePoint(230,1526,0,15,52),
            new SamplePoint(230,1526,0,15,52)

    };

    @Override
    public SamplePoint[] getPoints() {
        return points;
    }

    @Override
    public boolean isMove() {
        return false;
    }

    @Override
    public boolean isRestart() {
        return true;
    }

    @Override
    public boolean validate(BufferedImage bufferedImage) {
        return SamplePoint.validate(points, bufferedImage);
    }
}
