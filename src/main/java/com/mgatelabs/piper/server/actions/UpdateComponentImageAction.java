package com.mgatelabs.piper.server.actions;

import com.mgatelabs.piper.server.EditHolder;
import com.mgatelabs.piper.shared.details.ComponentDefinition;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.util.AdbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/22/2018.
 */
public class UpdateComponentImageAction implements EditActionInterface {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String execute(final String id, final String value, final EditHolder holder) {
        ComponentDefinition componentDefinition = holder.getComponentForId(id);
        if (componentDefinition == null) return "Could not find component with id: " + id;
        holder.getDeviceHelper().refresh(holder.getShell());
        ImageWrapper wrapper = holder.getDeviceHelper().download();
        if (wrapper != null && wrapper.isReady()) {
            File previewPath = ComponentDefinition.getPreviewPath(holder.getViewDefinition().getViewId(), componentDefinition.getComponentId());
            if (!wrapper.savePng(previewPath)) {
                logger.error("Failed to update component image");
                return ("Failed to update component image");
            } else {
                return ("Component image updated");
            }
        } else {
            return ("Could not obtain image from helper, is ADB connected and the helper running?");
        }
    }
}