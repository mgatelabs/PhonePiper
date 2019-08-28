package com.mgatelabs.piper.utils;

import com.mgatelabs.piper.shared.details.ScreenDefinition;
import com.mgatelabs.piper.shared.details.ViewDefinition;
import com.mgatelabs.piper.shared.image.SamplePoint;

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
            for (SamplePoint point : screenDefinition.getPoints()) {
                float xRate = point.getX() / 1080.0f;
                float yRate = point.getY() / 1920.0f;
                point.setX((int) (1920 * xRate));
                point.setY((int) (1080 * yRate));
            }
        }
        // Change the view ID
        viewDefinition.setViewId(args[1]);
        // Save this mess
        viewDefinition.save();
    }
}
