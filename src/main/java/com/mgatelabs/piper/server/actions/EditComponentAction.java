package com.mgatelabs.piper.server.actions;

import com.mgatelabs.piper.server.EditHolder;
import com.mgatelabs.piper.shared.details.ComponentDefinition;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.image.PngImageWrapper;
import com.mgatelabs.piper.ui.dialogs.ImagePixelPickerDialog;
import com.mgatelabs.piper.ui.dialogs.PickerHandler;

import java.util.logging.Logger;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/22/2018.
 */
public class EditComponentAction implements EditActionInterface {

    @Override
    public String execute(final String id, final String value, final EditHolder holder, Logger logger) {
        ComponentDefinition componentDefinition = holder.getComponentForId(id);
        if (componentDefinition == null) return "Could not find component with id: " + id;

        ImageWrapper imageWrapper = PngImageWrapper.getPngImage(ComponentDefinition.getPreviewPath(holder.getViewDefinition().getViewId(), componentDefinition.getComponentId()));

        if (imageWrapper == null) {
            return ("Local preview missing, please update the image first");
        }

        ImagePixelPickerDialog imagePixelPickerDialog = new ImagePixelPickerDialog(ImagePixelPickerDialog.Mode.BOX, null, new EditHandler(holder, componentDefinition, logger));

        int x = componentDefinition.getX() >= holder.getDeviceDefinition().getWidth() ? 0 : componentDefinition.getX();
        int y = componentDefinition.getY() >= holder.getDeviceDefinition().getHeight() ? 0 : componentDefinition.getY();

        int w = (x + componentDefinition.getW()) >= holder.getDeviceDefinition().getWidth() ? 10 : componentDefinition.getW();
        int h = (y + componentDefinition.getH()) >= holder.getDeviceDefinition().getHeight() ? 10 : componentDefinition.getH();

        imagePixelPickerDialog.setup(imageWrapper, x, y, w, h);
        imagePixelPickerDialog.start();


        return "";
    }

    public static class EditHandler implements PickerHandler {

        EditHolder holder;
        ComponentDefinition componentDefinition;
        Logger logger;

        public EditHandler(EditHolder holder, ComponentDefinition componentDefinition, Logger logger) {
            this.holder = holder;
            this.componentDefinition = componentDefinition;
            this.logger = logger;
        }

        @Override
        public void finished(ImagePixelPickerDialog imagePixelPickerDialog) {

            if (!imagePixelPickerDialog.isOk()) {
                logger.info("Stopping Edit Action");
                return;
            }

            if (imagePixelPickerDialog.getPoints().isEmpty()) {
                logger.info("You did not select any samples");
                return;
            } else if (imagePixelPickerDialog.getPoints().size() != 2) {
                logger.info("You must select 2 points");
            } else {

                int x1 = imagePixelPickerDialog.getPoints().get(0).getX();
                int x2 = imagePixelPickerDialog.getPoints().get(1).getX();
                int y1 = imagePixelPickerDialog.getPoints().get(0).getY();
                int y2 = imagePixelPickerDialog.getPoints().get(1).getY();

                if (x1 > x2) {
                    int temp = x1;
                    x1 = x2;
                    x2 = temp;
                }

                if (y1 > y2) {
                    int temp = y1;
                    y1 = y2;
                    y2 = temp;
                }

                componentDefinition.setEnabled(true);

                componentDefinition.setX(x1);
                componentDefinition.setY(y1);
                componentDefinition.setW(x2 - x1);
                componentDefinition.setH(y2 - y1);

                holder.getViewDefinition().sort();
                logger.info("Save Component: " + holder.getViewDefinition().save());
            }
        }
    }

}
