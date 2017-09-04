package com.mgatelabs.ffbe;

import com.mgatelabs.ffbe.shared.*;
import com.mgatelabs.ffbe.shared.details.DeviceDefinition;
import com.mgatelabs.ffbe.shared.image.ImageWrapper;
import com.mgatelabs.ffbe.shared.image.PngImageWrapper;
import com.mgatelabs.ffbe.ui.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class Runner {
  public static void main(String[] args) {
    GameRunner runner = new GameRunner();


    boolean showHelp = false;

    if (args.length >= 1) {
      if ("manage".equalsIgnoreCase(args[0])) {
        DeviceDefinition deviceDefinition = null;
        if (args.length == 2) {
          deviceDefinition = DeviceDefinition.read(args[1]);
        } else {
          deviceDefinition = DeviceDefinition.read("axon7");
        }
        if (deviceDefinition == null) {
          System.out.println("Could not find a device definition");
        }

        GameManager manager = new GameManager(deviceDefinition);

        manager.manage();
      } else if ("snap".equalsIgnoreCase(args[0])) {

        File sampleFile = new File("pieces//friendrequest.png");

        BufferedImage bufferedImage = null;

        try {
          bufferedImage = ImageIO.read(sampleFile);
        } catch (IOException e) {
          e.printStackTrace();
          return;
        }

        ImageWrapper rawImageWrapper = new PngImageWrapper(bufferedImage);// GameRunner.getScreen();

        ImagePixelPickerDialog dialog = new ImagePixelPickerDialog(ImagePixelPickerDialog.Mode.BOX);
        dialog.setup(rawImageWrapper, new ArrayList<>());
        dialog.start();

        //runner.snap();
        return;
      } else if ("mapper".equalsIgnoreCase(args[0])) {
        final String phoneName = args.length == 1 ? "axon7" : args[2];
        Phone phone = runner.loadPhone(phoneName);
        DungeonMapperCommandLine dungeonMapperCommandLine = new DungeonMapperCommandLine(phone);
        dungeonMapperCommandLine.run();
        return;
      } else if ("run".equalsIgnoreCase(args[0])) {
        if (args.length < 2) {
          showHelp = true;
        } else {
          final String scriptName = args[1];
          final String phoneName = args.length == 2 ? "axon7" : args[2];

          Script script = runner.loadScript(scriptName);

          Phone phone = runner.loadPhone(phoneName);

          if (phone != null && script != null) {
            runner.run(phone, script);
          } else if (phone == null) {
            System.out.println("Error: could not find phone with name " + phoneName);
          } else {
            System.out.println("Error: could not find script with name " + scriptName);
          }
        }
      }
    } else {
      showHelp = true;
    }

    if (showHelp) {
      System.out.println("Required Parameters missing");
      System.out.println("run (scriptName) [phoneName]");
      System.out.println("snap");
    }
  }
}
