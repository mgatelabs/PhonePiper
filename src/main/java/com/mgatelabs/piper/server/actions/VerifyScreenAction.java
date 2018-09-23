package com.mgatelabs.piper.server.actions;

import com.mgatelabs.piper.server.EditHolder;
import com.mgatelabs.piper.shared.details.ScreenDefinition;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.image.PngImageWrapper;
import com.mgatelabs.piper.shared.image.SamplePoint;

import java.io.File;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/22/2018.
 */
public class VerifyScreenAction implements EditActionInterface {
    @Override
    public String execute(final String id, final String value, final EditHolder holder) {
        ScreenDefinition screenDefinition = holder.getScreenForId(id);
        if (screenDefinition == null) return "Could not find screen with id: " + id;

        File previewPath = ScreenDefinition.getPreviewPath(holder.getViewDefinition().getViewId(), screenDefinition.getScreenId());

        ImageWrapper wrapper = PngImageWrapper.getPngImage(previewPath);

        if (wrapper != null && wrapper.isReady()) {
            if (SamplePoint.validate(screenDefinition.getPoints(), wrapper, false)) {
                return ("Validation: Success");
            } else {
                return ("Validation: Failed");
            }
        } else {
            return ("Local sample image doesn't exist, please update image first");
        }
    }
}
