package com.mgatelabs.ffbe.actions;

import com.mgatelabs.ffbe.shared.GameAction;
import com.mgatelabs.ffbe.shared.GameState;
import com.mgatelabs.ffbe.shared.SamplePoint;

import java.awt.image.BufferedImage;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class MenuFriendRequest extends GameAction {

    public MenuFriendRequest() {
        super(GameState.MENU, GameState.MENU, "Friend Request", "adb shell input tap 358 1874", 0);
    }

    private static final SamplePoint[] points = {
            // Auto Button
            new SamplePoint(97, 886, 253, 253, 253), new SamplePoint(97, 886, 253, 253, 253),
            new SamplePoint(97, 886, 253, 253, 253), new SamplePoint(97, 886, 253, 253, 253),
            new SamplePoint(138, 1841, 255, 255, 255), new SamplePoint(138, 1841, 255, 255, 255),
            new SamplePoint(138, 1841, 255, 255, 255), new SamplePoint(138, 1841, 255, 255, 255),
            new SamplePoint(200, 1855, 255, 255, 255), new SamplePoint(200, 1855, 255, 255, 255),
            new SamplePoint(200, 1855, 255, 255, 255), new SamplePoint(200, 1855, 255, 255, 255),
            new SamplePoint(230, 1889, 255, 255, 255), new SamplePoint(230, 1889, 255, 255, 255),
            new SamplePoint(230, 1889, 255, 255, 255), new SamplePoint(230, 1889, 255, 255, 255),
            new SamplePoint(272, 1842, 255, 255, 255), new SamplePoint(272, 1842, 255, 255, 255),
            new SamplePoint(272, 1842, 255, 255, 255), new SamplePoint(272, 1842, 255, 255, 255),
            new SamplePoint(342, 1840, 255, 255, 255), new SamplePoint(342, 1840, 255, 255, 255),
            new SamplePoint(342, 1840, 255, 255, 255), new SamplePoint(342, 1840, 255, 255, 255),
            new SamplePoint(383, 1873, 255, 255, 255), new SamplePoint(383, 1873, 255, 255, 255),
            new SamplePoint(383, 1873, 255, 255, 255), new SamplePoint(383, 1873, 255, 255, 255),
            new SamplePoint(466, 1859, 255, 255, 255), new SamplePoint(466, 1859, 255, 255, 255),
            new SamplePoint(466, 1859, 255, 255, 255), new SamplePoint(466, 1859, 255, 255, 255),
            new SamplePoint(506, 1874, 255, 255, 255), new SamplePoint(506, 1874, 255, 255, 255),
            new SamplePoint(506, 1874, 255, 255, 255), new SamplePoint(506, 1874, 255, 255, 255),
            new SamplePoint(567, 1858, 255, 255, 255), new SamplePoint(567, 1858, 255, 255, 255),
            new SamplePoint(567, 1858, 255, 255, 255), new SamplePoint(567, 1858, 255, 255, 255),
            new SamplePoint(1021, 1841, 255, 255, 255), new SamplePoint(1021, 1841, 255, 255, 255),
            new SamplePoint(1021, 1841, 255, 255, 255), new SamplePoint(1021, 1841, 255, 255, 255),
            new SamplePoint(1070, 1877, 255, 255, 255), new SamplePoint(1070, 1877, 255, 255, 255),
            new SamplePoint(1070, 1877, 255, 255, 255), new SamplePoint(1070, 1877, 255, 255, 255),
            new SamplePoint(1148, 1863, 255, 255, 255), new SamplePoint(1148, 1863, 255, 255, 255),
            new SamplePoint(1148, 1863, 255, 255, 255), new SamplePoint(1148, 1863, 255, 255, 255),
            new SamplePoint(1169, 1858, 255, 255, 255), new SamplePoint(1169, 1858, 255, 255, 255),
            new SamplePoint(1169, 1858, 255, 255, 255), new SamplePoint(1169, 1858, 255, 255, 255),
            new SamplePoint(1216, 1878, 255, 255, 255), new SamplePoint(1216, 1878, 255, 255, 255),
            new SamplePoint(1216, 1878, 255, 255, 255), new SamplePoint(1216, 1878, 255, 255, 255),
            new SamplePoint(1286, 1860, 255, 255, 255), new SamplePoint(1286, 1860, 255, 255, 255),
            new SamplePoint(1286, 1860, 255, 255, 255), new SamplePoint(1286, 1860, 255, 255, 255),
            new SamplePoint(1310, 1843, 255, 255, 255), new SamplePoint(1310, 1843, 255, 255, 255),
            new SamplePoint(1310, 1843, 255, 255, 255), new SamplePoint(1310, 1843, 255, 255, 255),
            new SamplePoint(804, 1952, 122, 122, 123), new SamplePoint(804, 1952, 122, 122, 123),
            new SamplePoint(804, 1952, 122, 122, 123), new SamplePoint(804, 1952, 122, 122, 123),
            new SamplePoint(985, 1051, 255, 255, 255), new SamplePoint(985, 1051, 255, 255, 255),
            new SamplePoint(985, 1051, 255, 255, 255), new SamplePoint(771, 1223, 193, 209, 235),
            new SamplePoint(771, 1223, 193, 209, 235), new SamplePoint(771, 1223, 193, 209, 235),
            new SamplePoint(771, 1223, 193, 209, 235)
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
