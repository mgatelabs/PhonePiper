package com.mgatelabs.piper.server.actions;

import com.mgatelabs.piper.server.EditHolder;
import com.mgatelabs.piper.shared.details.ScreenDefinition;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.image.PngImageWrapper;

import java.io.File;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/22/2018.
 */
public class CacheScreenAction implements EditActionInterface {

    @Override
    public String execute(final String id, final String value, final EditHolder holder) {
        ScreenDefinition screenDefinition = holder.getScreenForId(id);
        if (screenDefinition == null) return "Could not find screen with id: " + id;
        if (UpdateScreenAction.previousPreviewImage != null && UpdateScreenAction.previousPreviewImage.exists()) {
            ImageWrapper wrapper = PngImageWrapper.getPngImage(UpdateScreenAction.previousPreviewImage);
            File previewPath = ScreenDefinition.getPreviewPath(holder.getViewDefinition().getViewId(), screenDefinition.getScreenId());
            wrapper.savePng(previewPath);
            return ("Screen image updated");
        } else {
            return ("Failed to update screen image, have you download a image from the device?");
        }
    }
}
