package com.mgatelabs.piper.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mgatelabs.piper.shared.details.ScreenDefinition;
import com.mgatelabs.piper.shared.details.ViewDefinition;
import com.mgatelabs.piper.shared.image.SamplePoint;

import java.util.List;
import java.util.TreeSet;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2019 for Phone-Piper.
 */
public class FireReWrite {

    public static void main(String[] args) {
        ViewDefinition viewDefinition = ViewDefinition.read(args[0]);
        // Remove those pesky components
        viewDefinition.getComponents().clear();
        // Warp the screens
        for (ScreenDefinition screenDefinition : viewDefinition.getScreens()) {
            List<SamplePoint> extraPoints = Lists.newArrayList();
            for (SamplePoint point : screenDefinition.getPoints()) {
                final float xRate1 = point.getX() / 1080.0f;
                final float yRate1 = point.getY() / 1920.0f;

                final float xRate2;
                final float yRate2;

                if (point.getX() < 1079) {
                    xRate2 = (point.getX() + 1) / 1080.0f;
                }
                else {
                    xRate2 = (point.getX() - 1) / 1080.0f;
                }

                if (point.getY() < 1919) {
                    yRate2 = (point.getY() + 1) / 1920.0f;
                }
                else {
                    yRate2 = (point.getY() - 1) / 1920.0f;
                }

                int newX = (int) (1920 * ((xRate1 + xRate2) / 2.0));
                int newY = (int) (1080 * ((yRate1 + yRate2) / 2.0));

                for (int x = -1; x <= 1; x++) {
                    int offsetX = newX + x;
                    if (x >= 0 && x < 1920) {
                        extraPoints.add(new SamplePoint(offsetX, newY, point.getR(), point.getG(), point.getB()));
                    }
                }

            }
            screenDefinition.getPoints().clear();

            TreeSet<SamplePoint> sorter = Sets.newTreeSet();
            sorter.addAll(extraPoints);

            screenDefinition.getPoints().addAll(sorter);
        }
        // Change the view ID
        viewDefinition.setViewId(args[1]);
        // Save this mess
        viewDefinition.save();
    }
}
