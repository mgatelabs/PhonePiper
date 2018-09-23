package com.mgatelabs.piper.server.actions;

import com.mgatelabs.piper.server.EditHolder;
import com.mgatelabs.piper.shared.details.ScreenDefinition;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.image.SamplePoint;
import com.mgatelabs.piper.shared.util.AdbUtils;

import java.util.logging.Logger;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/22/2018.
 */
public class LiveVerifyScreenAction implements EditActionInterface {
    @Override
    public String execute(final String id, final String value, final EditHolder holder, Logger logger) {
        ScreenDefinition screenDefinition = holder.getScreenForId(id);
        if (screenDefinition == null) return "Could not find screen with id: " + id;

        AdbUtils.persistScreen(holder.getShell());
        ImageWrapper wrapper = holder.getDeviceHelper().download();
        //ImageWrapper wrapper = AdbUtils.getScreen();
        if (wrapper != null && wrapper.isReady()) {
            if (SamplePoint.validate(screenDefinition.getPoints(), wrapper, false)) {
                return ("Validation: Success");
            } else {
                return ("Validation: Failed");
            }
        } else {
            return ("Could not obtain image from helper, is ADB connected and the helper running?");
        }
    }
}
