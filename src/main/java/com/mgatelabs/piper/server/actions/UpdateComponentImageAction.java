package com.mgatelabs.piper.server.actions;

import com.mgatelabs.piper.server.EditHolder;
import com.mgatelabs.piper.shared.details.ComponentDefinition;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.util.AdbUtils;

import java.io.File;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/22/2018.
 */
public class UpdateComponentImageAction implements EditActionInterface {
    @Override
    public String execute(final String id, final String value, final EditHolder holder) {
        ComponentDefinition componentDefinition = holder.getComponentForId(id);
        if (componentDefinition == null) return "Could not find screen with id: " + id;
        AdbUtils.persistScreen(holder.getShell());
        ImageWrapper wrapper = holder.getDeviceHelper().download();
        if (wrapper != null && wrapper.isReady()) {
            File previewPath = ComponentDefinition.getPreviewPath(holder.getViewDefinition().getViewId(), componentDefinition.getComponentId());
            if (!wrapper.savePng(previewPath)) {
                return ("Failed to update component image");
            } else {
                return ("Component image updated");
            }
        } else {
            return ("Could not obtain image from helper, is ADB connected and the helper running?");
        }
    }
}