package com.mgatelabs.piper.server.actions;

import com.mgatelabs.piper.server.EditHolder;
import com.mgatelabs.piper.shared.details.ScreenDefinition;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.image.PngImageWrapper;
import com.mgatelabs.piper.shared.image.SamplePoint;
import com.mgatelabs.piper.ui.dialogs.ImagePixelPickerDialog;
import com.mgatelabs.piper.ui.dialogs.PickerHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/22/2018.
 */
public class EditScreenAction implements EditActionInterface {
    @Override
    public String execute(final String id, final String value, final EditHolder holder, Logger logger) {
        ScreenDefinition screenDefinition = holder.getScreenForId(id);
        if (screenDefinition == null) return "Could not find screen with id: " + id;

        File previewPath = ScreenDefinition.getPreviewPath(holder.getViewDefinition().getViewId(), screenDefinition.getScreenId());
        ImageWrapper wrapper = PngImageWrapper.getPngImage(previewPath);

        if (wrapper != null && wrapper.isReady()) {

            List<SamplePoint> copy = new ArrayList<>();
            copy.addAll(screenDefinition.getPoints());

            ImagePixelPickerDialog imagePixelPickerDialog = new ImagePixelPickerDialog(ImagePixelPickerDialog.Mode.PIXELS, null, new EditHandler(holder, screenDefinition, copy, logger));
            imagePixelPickerDialog.setup(wrapper, copy);
            imagePixelPickerDialog.start();

            return "";
        } else {
            return ("Local sample image doesn't exist, please update image first");
        }
    }

    public static class EditHandler implements PickerHandler {

        EditHolder holder;
        ScreenDefinition screenDefinition;
        Logger logger;
        List<SamplePoint> copy;

        public EditHandler(EditHolder holder, ScreenDefinition screenDefinition, List<SamplePoint> copy, Logger logger) {
            this.holder = holder;
            this.screenDefinition = screenDefinition;
            this.logger = logger;
            this.copy = copy;
        }

        @Override
        public void finished(ImagePixelPickerDialog imagePixelPickerDialog) {

            if (!imagePixelPickerDialog.isOk()) {
                logger.info("Stopping Edit Action");
                return;
            }

            if (imagePixelPickerDialog.getPoints().isEmpty()) {
                logger.info("Nothing selected, no action taken");
                return;
            } else {
                copy.clear();
                copy.addAll(imagePixelPickerDialog.getPoints());
            }

            screenDefinition.setEnabled(true);
            screenDefinition.getPoints().clear();
            screenDefinition.getPoints().addAll(copy);
            holder.getViewDefinition().sort();
            holder.getViewDefinition().save();
        }
    }
}
