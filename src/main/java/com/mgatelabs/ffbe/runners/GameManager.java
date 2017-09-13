package com.mgatelabs.ffbe.runners;

import com.mgatelabs.ffbe.shared.util.AdbShell;
import com.mgatelabs.ffbe.shared.util.AdbUtils;
import com.mgatelabs.ffbe.shared.util.ConsoleInput;
import com.mgatelabs.ffbe.shared.details.ActionType;
import com.mgatelabs.ffbe.shared.details.*;
import com.mgatelabs.ffbe.shared.image.ImageWrapper;
import com.mgatelabs.ffbe.shared.image.PngImageWrapper;
import com.mgatelabs.ffbe.shared.image.SamplePoint;
import com.mgatelabs.ffbe.ui.ImagePixelPickerDialog;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/1/2017.
 */
public class GameManager {

    DeviceDefinition deviceDefinition;

    ViewDefinition viewDefinition;

    Pattern pattern = Pattern.compile("^[a-zA-Z0-9-_]+$");

    private AdbShell shell;

    public GameManager(DeviceDefinition deviceDefinition) {
        this.deviceDefinition = deviceDefinition;
        viewDefinition = ViewDefinition.read(deviceDefinition.getViewId());

        shell = new AdbShell();
    }

    private void save() {
        viewDefinition.save(deviceDefinition.getViewId());
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    // GLOBAL
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public void manage() {

        if (viewDefinition == null) {
            System.out.println("Could not locate viewId: " + deviceDefinition.getViewId());
            return;
        }

        Collections.sort(viewDefinition.getScreens(), new Comparator<ScreenDefinition>() {
            @Override
            public int compare(ScreenDefinition o1, ScreenDefinition o2) {
                return o1.getScreenId().compareTo(o2.getScreenId());
            }
        });

        Collections.sort(viewDefinition.getComponents(), new Comparator<ComponentDefinition>() {
            @Override
            public int compare(ComponentDefinition o1, ComponentDefinition o2) {
                return o1.getComponentId().compareTo(o2.getComponentId());
            }
        });

        while (true) {

            System.out.println("----------");
            System.out.println("Manager: " + deviceDefinition.getName());
            System.out.println("----------");
            System.out.println();

            System.out.println("0: Exit Manager");
            System.out.println("1: Edit Screens");
            System.out.println("2: Edit Components");
            int command = ConsoleInput.getInt();


            switch (command) {
                case 1: {
                    manageScreens();
                }
                break;
                case 2: {
                    manageComponents();
                }
                break;
                case 0: {
                    return;
                }
            }
        }

    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    // SCREENS
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    private void manageScreens() {

        while (true) {

            System.out.println("----------");
            System.out.println("Screen Manager");
            System.out.println("----------");
            System.out.println();

            System.out.println("0: Return");
            System.out.println("1: New Screen");
            System.out.println("2: Edit Screen");

            int command = ConsoleInput.getInt();

            switch (command) {
                case 1: {
                    newScreen();
                }
                break;
                case 2: {
                    listScreens();
                }
                break;
                case 0: {
                    return;
                }
            }

        }
    }

    private void newScreen() {
        System.out.println("----------");
        System.out.println("New Screen");
        System.out.println("----------");
        System.out.println();

        System.out.println("Enter screen name: ");

        String name = ConsoleInput.getString();

        if (name.isEmpty()) {
            System.out.println("No input, stopping");
            return;
        }

        String id;
        while (true) {
            System.out.println("Enter screen id: (a-z A-Z 0-9 - _)");
            id = ConsoleInput.getString();
            if (id.isEmpty()) {
                System.out.println("No input, stopping");
                return;
            }
            if (!pattern.matcher(id).matches()) {
                System.out.println("Invalid id format, please try again");
                continue;
            }

            boolean duplicate = false;
            for (ScreenDefinition screenDefinition : viewDefinition.getScreens()) {
                if (screenDefinition.getScreenId().equalsIgnoreCase(id)) {
                    System.out.println("Duplicate screen found, please try again");
                    duplicate = true;
                    break;
                }
            }
            if (!duplicate) {
                break;
            }
        }

        System.out.println("Getting ready to take screen shot, Enter 1 to continue: ");

        if (ConsoleInput.getInt() != 1) return;

        System.out.println("Starting capture");

        ImageWrapper imageReader;

        while (true) {
            imageReader = AdbUtils.getScreen();
            if (imageReader == null || !imageReader.isReady()) {
                System.out.println("Something went wrong, try again? (y/n)");
                if (!ConsoleInput.yesNo()) {
                    System.out.println("Stopping");
                    return;
                }
            } else {
                break;
            }
        }

        List<SamplePoint> samples = new ArrayList<>();

        while (true) {
            ImagePixelPickerDialog imagePixelPickerDialog = new ImagePixelPickerDialog(ImagePixelPickerDialog.Mode.PIXELS);
            imagePixelPickerDialog.setup(imageReader, samples);
            imagePixelPickerDialog.start();

            if (!imagePixelPickerDialog.isOk()) {
                System.out.println("Stopping");
                return;
            } else if (imagePixelPickerDialog.getPoints().isEmpty()) {
                System.out.println("You did not select any samples, try again: (y/n)");
                if (!ConsoleInput.yesNo()) {
                    System.out.println("Stopping");
                    return;
                }
            } else {
                samples.addAll(imagePixelPickerDialog.getPoints());
                break;
            }
        }

        System.out.println("Save screen: " + id + "(y/n)");
        if (ConsoleInput.yesNo()) {

            ScreenDefinition screenDefinition = new ScreenDefinition();
            screenDefinition.setScreenId(id);
            screenDefinition.setName(name);
            screenDefinition.setPoints(samples);

            viewDefinition.getScreens().add(screenDefinition);

            imageReader.savePng(ScreenDefinition.getPreviewPath(deviceDefinition.getViewId(), id));

            save();
        }
    }

    private void listScreens() {

        while (true) {

            System.out.println("----------");
            System.out.println("Screen List:");
            System.out.println("----------");
            System.out.println();

            System.out.println("0: Stop");

            for (int i = 0; i < viewDefinition.getScreens().size(); i++) {
                System.out.println((i + 1) + ": " + viewDefinition.getScreens().get(i).getScreenId() + " - " + viewDefinition.getScreens().get(i).getName());
            }
            int command = ConsoleInput.getInt();
            if (command <= 0) return;
            if (command >= 1 && command <= viewDefinition.getScreens().size()) {
                manageScreen(command - 1);
            }
        }
    }

    private void manageScreen(int screenIndex) {

        ScreenDefinition screenDefinition = viewDefinition.getScreens().get(screenIndex);

        File previewPath = ScreenDefinition.getPreviewPath(deviceDefinition.getViewId(), screenDefinition.getScreenId());

        while (true) {

            System.out.println("----------");
            System.out.println("Screen Options: " + screenDefinition.getScreenId() + " (" + screenDefinition.getName() + ")");
            System.out.println("----------");
            System.out.println();

            System.out.println("0: Stop");

            System.out.println("1: Verify (Live Image)");
            if (previewPath.exists()) {
                System.out.println("2: Verify (Saved Image)");
                System.out.println("3: Edit Points (Saved Image)");
            }
            System.out.println("4: Update Image");
            System.out.println("5: Change Name");
            System.out.println("7: Delete Screen");

            int command = ConsoleInput.getInt();
            if (command <= 0) return;

            switch (command) {
                case 1: {
                    verifyScreenImage(screenDefinition, true);
                }
                break;
                case 2: {
                    verifyScreenImage(screenDefinition, false);
                }
                break;
                case 3: {
                    updateScreenPoints(screenDefinition);
                }
                break;
                case 4: {
                    updateScreenImage(screenDefinition);
                }
                break;
                case 5: {
                    changeScreenName(screenDefinition);
                }
                break;
                case 7: {
                    System.out.println("Are you sure? (Y/N)");
                    if (ConsoleInput.yesNo()) {
                        // Delete
                        viewDefinition.getScreens().remove(screenIndex);
                        return;
                    }
                }
                break;
            }

        }

    }

    public void changeScreenName(ScreenDefinition screenDefinition) {

        System.out.println("New name: ");

        String newName = ConsoleInput.getString();

        if (newName == null || newName.isEmpty()) {
            System.out.println("Stopping");
            return;
        }
        screenDefinition.setName(newName);

        save();
    }

    public void verifyScreenImage(ScreenDefinition definition, boolean live) {

        ImageWrapper imageWrapper;

        if (live) {
            imageWrapper = getLiveImage();
        } else {
            imageWrapper = getPngImage(ScreenDefinition.getPreviewPath(deviceDefinition.getViewId(), definition.getScreenId()));
        }

        if (imageWrapper == null) {
            System.out.println("Could not verify points, image missing");
            return;
        }

        System.out.print("Status: ");
        if (SamplePoint.validate(definition.getPoints(), imageWrapper)) {
            System.out.println("Valid");
        } else {
            System.out.println("Invalid");
        }
    }

    public ImageWrapper getPngImage(File path) {
        if (!path.exists()) return null;
        try {
            final BufferedImage bufferedImage = ImageIO.read(path);
            return new PngImageWrapper(bufferedImage);
        } catch (Exception ex) {
            return null;
        }
    }

    public void updateScreenImage(ScreenDefinition screenDefinition) {

        ImageWrapper imageWrapper;

        while (true) {
            imageWrapper = AdbUtils.getScreen();
            if (imageWrapper == null || !imageWrapper.isReady()) {
                System.out.println("Something went wrong, try again? (y/n)");
                if (!ConsoleInput.yesNo()) {
                    System.out.println("Stopping");
                    return;
                }
            } else {
                break;
            }
        }

        File previewPath = ScreenDefinition.getPreviewPath(deviceDefinition.getViewId(), screenDefinition.getScreenId());

        imageWrapper.savePng(previewPath);

        System.out.println("Image updated");
    }

    public void updateScreenPoints(ScreenDefinition screenDefinition) {

        ImageWrapper imageWrapper = getPngImage(ScreenDefinition.getPreviewPath(deviceDefinition.getViewId(), screenDefinition.getScreenId()));

        if (imageWrapper == null) {
            System.out.println("Image not found, stopping");
            return;
        }

        List<SamplePoint> copy = new ArrayList<>();
        copy.addAll(screenDefinition.getPoints());

        while (true) {
            ImagePixelPickerDialog imagePixelPickerDialog = new ImagePixelPickerDialog(ImagePixelPickerDialog.Mode.PIXELS);
            imagePixelPickerDialog.setup(imageWrapper, copy);
            imagePixelPickerDialog.start();

            if (!imagePixelPickerDialog.isOk()) {
                System.out.println("Stopping");
                return;
            }

            if (imagePixelPickerDialog.getPoints().isEmpty()) {
                System.out.println("You did not select any samples, try again: (y/n)");
                if (!ConsoleInput.yesNo()) {
                    System.out.println("Stopping");
                    return;
                }
            } else {
                copy.clear();
                copy.addAll(imagePixelPickerDialog.getPoints());
                break;
            }
        }

        screenDefinition.getPoints().clear();
        screenDefinition.getPoints().addAll(copy);

        save();

        System.out.println("Screen updated");
    }

    public ImageWrapper getLiveImage() {
        ImageWrapper imageWrapper;
        while (true) {
            imageWrapper = AdbUtils.getScreen();
            if (imageWrapper == null || !imageWrapper.isReady()) {
                System.out.println("Something went wrong, try again? (y/n)");
                if (!ConsoleInput.yesNo()) {
                    System.out.println("Stopping");
                    return null;
                }
            } else {
                return imageWrapper;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    // COMPONENTS
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    private void manageComponents() {
        while (true) {
            System.out.println("----------");
            System.out.println("Component Manager");
            System.out.println("----------");
            System.out.println();

            System.out.println("0: Return");
            System.out.println("1: New Component");
            System.out.println("2: Edit Component");


            int command = ConsoleInput.getInt();

            switch (command) {
                case 1: {
                    newComponent();
                }
                break;
                case 2: {
                    listComponents();
                }
                break;
                case 0: {
                    return;
                }
            }

        }
    }

    private void newComponent() {
        System.out.println("----------");
        System.out.println("New Component");
        System.out.println("----------");
        System.out.println();

        System.out.println("Enter component name: ");

        String name = ConsoleInput.getString();

        if (name.isEmpty()) {
            System.out.println("No input, stopping");
            return;
        }

        String id;
        while (true) {
            System.out.println("Enter component id: (a-z A-Z 0-9 - _)");
            id = ConsoleInput.getString();
            if (id.isEmpty()) {
                System.out.println("No input, stopping");
                return;
            }
            if (!pattern.matcher(id).matches()) {
                System.out.println("Invalid id format, please try again");
                continue;
            }

            boolean duplicate = false;
            for (ComponentDefinition componentDefinition : viewDefinition.getComponents()) {
                if (componentDefinition.getComponentId().equalsIgnoreCase(id)) {
                    System.out.println("Duplicate component found, please try again");
                    duplicate = true;
                    break;
                }
            }
            if (!duplicate) {
                break;
            }
        }

        System.out.println("Getting ready to take screen shot, Enter 1 to continue: ");

        if (ConsoleInput.getInt() != 1) return;

        System.out.println("Starting capture");

        ImageWrapper imageReader;

        while (true) {
            imageReader = AdbUtils.getScreen();
            if (imageReader == null || !imageReader.isReady()) {
                System.out.println("Something went wrong, try again? (y/n)");
                if (!ConsoleInput.yesNo()) {
                    System.out.println("Stopping");
                    return;
                }
            } else {
                break;
            }
        }

        ComponentDefinition componentDefinition = new ComponentDefinition();
        componentDefinition.setComponentId(id);
        componentDefinition.setName(name);

        while (true) {
            ImagePixelPickerDialog imagePixelPickerDialog = new ImagePixelPickerDialog(ImagePixelPickerDialog.Mode.BOX);
            imagePixelPickerDialog.setup(imageReader, new ArrayList<>());
            imagePixelPickerDialog.start();

            if (!imagePixelPickerDialog.isOk()) {
                System.out.println("Stopping");
                return;
            } else if (imagePixelPickerDialog.getPoints().isEmpty()) {
                System.out.println("You did not select any samples, try again: (y/n)");
                if (!ConsoleInput.yesNo()) {
                    System.out.println("Stopping");
                    return;
                }
            } else if (imagePixelPickerDialog.getPoints().size() != 2) {
                System.out.println("You must select 2 points, try again: (y/n)");
                if (!ConsoleInput.yesNo()) {
                    System.out.println("Stopping");
                    return;
                }
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

                componentDefinition.setX(x1);
                componentDefinition.setY(y1);
                componentDefinition.setW(x2 - x1);
                componentDefinition.setH(y2 - y1);

                break;
            }
        }

        System.out.println("Save component: " + id + "(y/n)");
        if (ConsoleInput.yesNo()) {
            viewDefinition.getComponents().add(componentDefinition);
            imageReader.savePng(ComponentDefinition.getPreviewPath(deviceDefinition.getViewId(), id));
            save();
        }
    }

    private void listComponents() {

        while (true) {

            System.out.println("----------");
            System.out.println("Component List:");
            System.out.println("----------");
            System.out.println();

            System.out.println("0: Stop");

            for (int i = 0; i < viewDefinition.getComponents().size(); i++) {
                System.out.println((i + 1) + ": " + viewDefinition.getComponents().get(i).getComponentId() + " - " + viewDefinition.getComponents().get(i).getName());
            }
            int command = ConsoleInput.getInt();
            if (command <= 0) return;
            if (command >= 1 && command <= viewDefinition.getComponents().size()) {
                manageComponent(command - 1);
            }
        }
    }

    private void manageComponent(int componentIndex) {

        ComponentDefinition componentDefinition = viewDefinition.getComponents().get(componentIndex);

        File previewPath = ComponentDefinition.getPreviewPath(deviceDefinition.getViewId(), componentDefinition.getComponentId());

        while (true) {

            System.out.println("----------");
            System.out.println("Component Options: " + componentDefinition.getComponentId() + " (" + componentDefinition.getName() + ")");
            System.out.println("----------");
            System.out.println();

            System.out.println("0: Stop");
            if (previewPath.exists()) {
                System.out.println("1: Edit Shape (Saved Image)");
            }
            System.out.println("2: Update Image");
            System.out.println("3: Change Name");
            System.out.println("4: Tap");
            System.out.println("5: Swipe Right");
            System.out.println("6: Swipe Down");
            System.out.println("666: Delete Screen");

            int command = ConsoleInput.getInt();
            if (command <= 0) return;

            switch (command) {
                case 1: {
                    updateComponentPoints(componentDefinition);
                }
                break;
                case 2: {
                    updateComponentImage(componentDefinition);
                }
                break;
                case 3: {
                    changeComponentName(componentDefinition);
                }
                break;
                case 4: {
                    System.out.println("Sending Tap");
                    AdbUtils.component(componentDefinition, ActionType.TAP, shell, false);
                }
                break;

                case 5: {
                    System.out.println("Sending Swipe Right");
                    AdbUtils.component(componentDefinition, ActionType.SWIPE_RIGHT, shell, false);
                }
                break;
                case 6: {
                    System.out.println("Sending Down");
                    AdbUtils.component(componentDefinition, ActionType.SWIPE_DOWN, shell, false);
                }
                break;

                case 666: {
                    System.out.println("Are you sure? (Y/N)");
                    if (ConsoleInput.yesNo()) {
                        // Delete
                        viewDefinition.getComponents().remove(componentIndex);
                        return;
                    }
                }
                break;
            }

        }

    }

    public void updateComponentPoints(ComponentDefinition componentDefinition) {

        ImageWrapper imageWrapper = getPngImage(ComponentDefinition.getPreviewPath(deviceDefinition.getViewId(), componentDefinition.getComponentId()));

        if (imageWrapper == null) {
            System.out.println("Image not found, stopping");
            return;
        }

        while (true) {
            ImagePixelPickerDialog imagePixelPickerDialog = new ImagePixelPickerDialog(ImagePixelPickerDialog.Mode.BOX);
            imagePixelPickerDialog.setup(imageWrapper, componentDefinition.getX(), componentDefinition.getY(), componentDefinition.getW(), componentDefinition.getH());
            imagePixelPickerDialog.start();

            if (!imagePixelPickerDialog.isOk()) {
                System.out.println("Stopping");
                return;
            }

            if (imagePixelPickerDialog.getPoints().isEmpty()) {
                System.out.println("You did not select any samples, try again: (y/n)");
                if (!ConsoleInput.yesNo()) {
                    System.out.println("Stopping");
                    return;
                }
            } else if (imagePixelPickerDialog.getPoints().size() != 2) {
                System.out.println("You must select 2 points, try again: (y/n)");
                if (!ConsoleInput.yesNo()) {
                    System.out.println("Stopping");
                    return;
                }
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

                componentDefinition.setX(x1);
                componentDefinition.setY(y1);
                componentDefinition.setW(x2 - x1);
                componentDefinition.setH(y2 - y1);

                break;
            }
        }


        save();

        System.out.println("Component updated");
    }

    public void updateComponentImage(ComponentDefinition componentDefinition) {

        ImageWrapper imageWrapper;

        while (true) {
            imageWrapper = AdbUtils.getScreen();
            if (imageWrapper == null || !imageWrapper.isReady()) {
                System.out.println("Something went wrong, try again? (y/n)");
                if (!ConsoleInput.yesNo()) {
                    System.out.println("Stopping");
                    return;
                }
            } else {
                break;
            }
        }

        File previewPath = ComponentDefinition.getPreviewPath(deviceDefinition.getViewId(), componentDefinition.getComponentId());

        imageWrapper.savePng(previewPath);

        System.out.println("Image updated");
    }

    public void changeComponentName(ComponentDefinition componentDefinition) {

        System.out.println("New name: ");

        String newName = ConsoleInput.getString();

        if (newName == null || newName.isEmpty()) {
            System.out.println("Stopping");
            return;
        }
        componentDefinition.setName(newName);

        save();
    }
}
