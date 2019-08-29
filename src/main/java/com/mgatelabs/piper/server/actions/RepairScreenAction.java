package com.mgatelabs.piper.server.actions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mgatelabs.piper.server.EditHolder;
import com.mgatelabs.piper.shared.details.ScreenDefinition;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.image.PngImageWrapper;
import com.mgatelabs.piper.shared.image.SamplePoint;
import com.mgatelabs.piper.shared.image.Sampler;
import com.mgatelabs.piper.ui.dialogs.ImagePixelPickerDialog;

import java.io.File;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/22/2018.
 */
public class RepairScreenAction implements EditActionInterface {
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
                } else {

                    final int origX = oldPoint.getX();
                    final int origY = oldPoint.getY();

                    SamplePoint bestPoint = null;
                    int bestScore = -1;

                    for (int y = -4; y <= 4; y++) {
                        int newY = origY + y;
                        for (int x = -4; x <= 4; x++) {
                            int newX = origX + x;
                            oldPoint.setX(newX);
                            oldPoint.setY(newY);
                            if (SamplePoint.validate(ImmutableList.of(oldPoint), wrapper, false)) {
                                int pointScore = determineValue(wrapper, oldPoint);
                                if (pointScore > bestScore) {
                                    bestScore = pointScore;
                                    bestPoint = new SamplePoint(oldPoint);
                                }
                            }
                        }
                    }

                    if (bestScore > 0) {
                        newPoints.add(bestPoint);
                    }

                }
            }

            TreeSet<SamplePoint> cleaner = Sets.newTreeSet();
            cleaner.addAll(newPoints);
            newPoints.clear();
            newPoints.addAll(cleaner);

            ImagePixelPickerDialog imagePixelPickerDialog = new ImagePixelPickerDialog(ImagePixelPickerDialog.Mode.PIXELS, null, new FixScreenAction.EditHandler(holder, screenDefinition, newPoints));
            imagePixelPickerDialog.setup(wrapper, newPoints);
            imagePixelPickerDialog.start();

            return "";
        } else {
            return ("Local sample image doesn't exist, please update image first");
        }
    }

    public static int determineValue(ImageWrapper imageWrapper, final SamplePoint center) {

        int cx = center.getX();
        int cy = center.getY();

        Sampler sample = new Sampler();

        int value = 0;

        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                imageWrapper.getPixel(cx + x, cy + y, sample);

                int diffR = center.getR() - sample.getR();
                int diffG = center.getG() - sample.getG();
                int diffB = center.getB() - sample.getB();
                if (diffR < 0) diffR *= -1;
                if (diffG < 0) diffG *= -1;
                if (diffB < 0) diffB *= -1;

                if (x == 0 && y == 0) {

                } else {
                    if (diffR <= 6 && diffG <= 6 && diffB <= 6) {
                        value += 3;
                    }
                }
            }
        }

        return value;
    }

}
