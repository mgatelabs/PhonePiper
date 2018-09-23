package com.mgatelabs.piper.server.actions;

import com.mgatelabs.piper.server.EditHolder;
import com.mgatelabs.piper.shared.details.ComponentDefinition;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.image.PngImageWrapper;
import com.mgatelabs.piper.ui.dialogs.ImagePixelPickerDialog;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/22/2018.
 */
public class EditComponentAction implements EditActionInterface {

    @Override
    public String execute(final String id, final String value, final EditHolder holder) {
        ComponentDefinition componentDefinition = holder.getComponentForId(id);
        if (componentDefinition == null) return "Could not find component with id: " + id;

        /*
        ImageWrapper imageWrapper = PngImageWrapper.getPngImage(ComponentDefinition.getPreviewPath(holder.getViewDefinition().getViewId(), componentDefinition.getComponentId()));

        if (imageWrapper == null) {
            return ("Local preview missing, please update the image first");
        }

        ImagePixelPickerDialog imagePixelPickerDialog = new ImagePixelPickerDialog(ImagePixelPickerDialog.Mode.BOX, null);

        int x = componentDefinition.getX() >= holder.getDeviceDefinition().getWidth() ? 0 : componentDefinition.getX();
        int y = componentDefinition.getY() >= holder.getDeviceDefinition().getHeight() ? 0 : componentDefinition.getY();

        int w = (x + componentDefinition.getW()) >= holder.getDeviceDefinition().getWidth() ? 10 : componentDefinition.getW();
        int h = (y + componentDefinition.getH()) >= holder.getDeviceDefinition().getHeight() ? 10 : componentDefinition.getH();

        imagePixelPickerDialog.setup(imageWrapper, x, y, w, h);
        imagePixelPickerDialog.start();
        */

        return "Not Ready";
    }

}
