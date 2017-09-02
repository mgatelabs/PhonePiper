package com.mgatelabs.ffbe.shared;

import com.mgatelabs.ffbe.GameRunner;
import com.mgatelabs.ffbe.shared.details.DeviceDefinition;
import com.mgatelabs.ffbe.shared.details.ScreenDefinition;
import com.mgatelabs.ffbe.shared.details.ViewDefinition;
import com.mgatelabs.ffbe.shared.image.RawImageReader;
import com.mgatelabs.ffbe.ui.ImagePixelPickerDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/1/2017.
 */
public class GameManager {

    DeviceDefinition deviceDefinition;

    ViewDefinition viewDefinition;

    Pattern pattern = Pattern.compile("^[a-zA-Z0-9-_]+$");

    public GameManager(DeviceDefinition deviceDefinition) {
        this.deviceDefinition = deviceDefinition;
        viewDefinition = ViewDefinition.read(deviceDefinition.getViewId());
    }

    public void manage() {

        if (viewDefinition == null) {
            System.out.println("Could not locate viewId: " + deviceDefinition.getViewId());
            return;
        }

        while (true) {

            System.out.println("----------");
            System.out.println("Manager: " + deviceDefinition.getName());
            System.out.println("----------");
            System.out.println();

            System.out.println("1: Edit Screens");
            System.out.println("2: Edit Components");
            System.out.println("9: Exit Manager");
            int command = ConsoleInput.getInt();


            switch (command) {
                case 1: {
                    manageScreens();
                } break;
                case 2: {
                    manageComponents();
                } break;
                case  9: {
                    return;
                }
            }
        }

    }

    private void manageScreens() {

        while (true) {

            System.out.println("----------");
            System.out.println("Screen Manager");
            System.out.println("----------");
            System.out.println();

            System.out.println("1: New Screen");
            System.out.println("2: Edit Screen");
            System.out.println("9: Return");

            int command = ConsoleInput.getInt();

            switch (command) {
                case 1: {
                    newScreen();
                } break;
                case 2: {

                } break;
                case  9: {
                    return;
                }
            }

        }
    }

    private void save() {
        viewDefinition.save(deviceDefinition.getViewId());
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
            for (ScreenDefinition screenDefinition: viewDefinition.getScreens()) {
                if (screenDefinition.getComponentId().equalsIgnoreCase(id)) {
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

        RawImageReader rawImageReader;

        while (true) {
            rawImageReader = GameRunner.getScreen();
            if (rawImageReader == null || !rawImageReader.isReady()) {
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
            ImagePixelPickerDialog imagePixelPickerDialog = new ImagePixelPickerDialog();
            imagePixelPickerDialog.setup(rawImageReader, samples);
            imagePixelPickerDialog.start();

            if (samples.isEmpty()) {
                System.out.println("You did not select any samples, try again: (y/n)");
                if (ConsoleInput.yesNo()) {
                    System.out.println("Stopping");
                    return;
                }
            } else {
                break;
            }
        }

        System.out.println("Save screen: " + id);
        if (ConsoleInput.yesNo()) {

            ScreenDefinition screenDefinition = new ScreenDefinition();
            screenDefinition.setComponentId(id);
            screenDefinition.setPoints(samples);

            viewDefinition.getScreens().add(screenDefinition);

            rawImageReader.savePng(new File("views/" + deviceDefinition.getViewId() + "/s-" + id + ".png"));

            save();
        }
    }

    private void manageComponents() {
        while (true) {
            System.out.println("----------");
            System.out.println("Component Manager");
            System.out.println("----------");
            System.out.println();

            System.out.println("1: New Component");
            System.out.println("2: Edit Component");
            System.out.println("9: Return");

            int command = ConsoleInput.getInt();

            switch (command) {
                case 1: {

                } break;
                case 2: {

                } break;
                case  9: {
                    return;
                }
            }

        }
    }
}
