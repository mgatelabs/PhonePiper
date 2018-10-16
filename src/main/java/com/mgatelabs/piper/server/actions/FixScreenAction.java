package com.mgatelabs.piper.server.actions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mgatelabs.piper.server.EditHolder;
import com.mgatelabs.piper.shared.details.ScreenDefinition;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.image.PngImageWrapper;
import com.mgatelabs.piper.shared.image.SamplePoint;
import com.mgatelabs.piper.ui.dialogs.ImagePixelPickerDialog;
import com.mgatelabs.piper.ui.dialogs.PickerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/22/2018.
 */
public class FixScreenAction implements EditActionInterface {
    @Override
    public String execute(final String id, final String value, final EditHolder holder) {
        ScreenDefinition screenDefinition = holder.getScreenForId(id);
        if (screenDefinition == null) return "Could not find screen with id: " + id;

        File previewPath = ScreenDefinition.getPreviewPath(holder.getViewDefinition().getViewId(), screenDefinition.getScreenId());
        ImageWrapper wrapper = PngImageWrapper.getPngImage(previewPath);

        if (wrapper != null && wrapper.isReady()) {

            List<SamplePoint> newPoints = Lists.newArrayList();

            for (SamplePoint oldPoint : screenDefinition.getPoints()) {
                if (oldPoint.getX() >= holder.getDeviceDefinition().getWidth() || oldPoint.getY() >= holder.getDeviceDefinition().getHeight()) {
                    continue;
                }
                if (SamplePoint.validate(ImmutableList.of(oldPoint), wrapper, false)) {
                    newPoints.add(oldPoint);
                }
            }

            ImagePixelPickerDialog imagePixelPickerDialog = new ImagePixelPickerDialog(ImagePixelPickerDialog.Mode.PIXELS, null, new EditHandler(holder, screenDefinition, newPoints));
            imagePixelPickerDialog.setup(wrapper, newPoints);
            imagePixelPickerDialog.start();

            return "";
        } else {
            return ("Local sample image doesn't exist, please update image first");
        }
    }

    public static class EditHandler implements PickerHandler {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());
        private final EditHolder holder;
        private final ScreenDefinition screenDefinition;
        private final List<SamplePoint> copy;

        public EditHandler(EditHolder holder, ScreenDefinition screenDefinition, List<SamplePoint> copy) {
            this.holder = holder;
            this.screenDefinition = screenDefinition;
            this.copy = copy;
        }

        @Override
        public void finished(ImagePixelPickerDialog imagePixelPickerDialog) {

            if (!imagePixelPickerDialog.isOk()) {
                logger.info("Stopping Edit Action");
                return;
            }

            if (imagePixelPickerDialog.getPoints().isEmpty()) {
                logger.info("Nothing selected");
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
