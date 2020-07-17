package com.mgatelabs.piper.server.actions;

import com.mgatelabs.piper.server.EditHolder;
import com.mgatelabs.piper.shared.details.ScreenDefinition;
import com.mgatelabs.piper.shared.helper.LocalDeviceHelper;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.image.SamplePoint;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/22/2018.
 */
public class LiveVerifyScreenAction implements EditActionInterface {
    @Override
    public String execute(final String id, final String value, final EditHolder holder) {
        ScreenDefinition screenDefinition = holder.getScreenForId(id);
        if (screenDefinition == null) return "Could not find screen with id: " + id;
        holder.getDeviceHelper().refresh(holder.getShell(),  holder.getDeviceHelper() instanceof LocalDeviceHelper ? 3 : 0);
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
