package com.mgatelabs.piper.server.actions;

import com.mgatelabs.piper.server.EditHolder;
import com.mgatelabs.piper.shared.details.ScreenDefinition;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.util.AdbUtils;

import java.io.File;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/22/2018.
 */
public class UpdateScreenAction implements EditActionInterface {

    public static File previousPreviewImage = null;

    @Override
    public String execute(final String id, final String value, final EditHolder holder) {
        ScreenDefinition screenDefinition = holder.getScreenForId(id);
        if (screenDefinition == null) return "Could not find screen with id: " + id;
        holder.getDeviceHelper().makeReady(holder.getShell()).refresh(holder.getShell());
        ImageWrapper wrapper = holder.getDeviceHelper().download();
        if (wrapper != null && wrapper.isReady()) {
            File previewPath = ScreenDefinition.getPreviewPath(holder.getViewDefinition().getViewId(), screenDefinition.getScreenId());
            if (!wrapper.savePng(previewPath)) {
                return ("Failed to update screen image");
            } else {
                previousPreviewImage = previewPath;
                return ("Screen image updated");
            }
        } else {
            return ("Could not obtain image from helper, is ADB connected and the helper running?");
        }
    }
}
