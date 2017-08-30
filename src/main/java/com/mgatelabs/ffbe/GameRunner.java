package com.mgatelabs.ffbe;

import com.fasterxml.jackson.databind.*;
import com.mgatelabs.ffbe.shared.*;

import java.awt.image.*;
import java.io.*;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/27/2017.
 */
public class GameRunner {

  private static final File OutputImage = new File("output.png");

  public void run(Phone phone, Script script) {

    final List<ActionSet> sets = new ArrayList<>();
    ActionSet set;

    for (SetDetail setDetail : script.getSets()) {
      set = new ActionSet(setDetail.getTitle(), setDetail.getWait());
      for (String actionId : setDetail.getActions()) {
        ActionDetail actionDetail = script.getActions().get(actionId);
        GameAction gameAction = new GameAction(GameState.MENU, GameState.MENU, actionDetail.getOutcome(), actionDetail.getScreen(), actionDetail.getWait(), actionDetail.getFlow());
        ScreenDetail screenDetail = phone.getScreens().get(actionDetail.getScreen());
        gameAction.setPoints(screenDetail.getPoints());

        for (CommandDetail commandDetail : actionDetail.getCommands()) {
          switch (commandDetail.getType()) {
            case TAP: {
              ButtonLocation button = screenDetail.getButtons().get(commandDetail.getId());
              CommandAction action = new CommandAction(ActionType.TAP, button.getX(), button.getY());
              gameAction.addAction(action);
            }
            break;
          }
        }

        set.addAction(gameAction);
      }
      sets.add(set);
    }

    runSets(sets);
  }

  private void runSets(List<ActionSet> sets) {
    int currentSetIndex = 0;
    int maxLoops = 100;

    SecureRandom secureRandom = new SecureRandom();

    // Start with a Screen shot

    while (maxLoops > 0) {
      BufferedImage bufferedImage = getScreen(OutputImage);
      ActionSet set = sets.get(currentSetIndex);

      int waitTime = set.getWaitTime();

      System.out.println("Running Set: " + set.getTitle());

      for (GameAction action : set.getPossibleActions()) {
        System.out.println("Testing Action: " + action.getTitle());
        if (action.validate(bufferedImage)) {
          System.out.println("Action Passed");
          switch (action.getCommandMode()) {
            case FLOW: {
              for (CommandAction commandAction : action.getActions()) {
                runAction(commandAction, secureRandom);
              }
            }
            break;
            case RANDOM: {
              int index = secureRandom.nextInt(action.getActions().size());
              runAction(action.getActions().get(index), secureRandom);
            }
            break;
          }
          // Let it override the wait time
          if (action.getWaitTime() > 0) {
            waitTime = action.getWaitTime();
          }
          if (action.isMove()) {
            System.out.println("Moving to Next ActionSet");
            currentSetIndex = (currentSetIndex + 1) % sets.size();
            if (currentSetIndex == 0) {
              maxLoops--;
            }
            break;
          } else if (action.isRestart()) {
            System.out.println("Restarting Action");
            break; // Stop processing
          }
        }
      }

      try {
        Thread.sleep(waitTime);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private void runAction(CommandAction action, SecureRandom random) {
    switch (action.getType()) {
      case TAP: {
        int x = action.getX();
        int y = action.getY();
        x += random.nextInt() % 4;
        y += random.nextInt() % 4;
        exec("adb shell input tap " + x + " " + y);
      }
      break;
    }
  }

  public static BufferedImage getScreen(File imageFile) {
    if (imageFile.exists()) {
      imageFile.delete();
    }
    if (exec("adb shell screencap -p /mnt/sdcard/output.png") && exec("adb pull /mnt/sdcard/output.png " + imageFile.getName()) && exec("adb shell rm /mnt/sdcard/output.png")) {
      return ImageUtil.readImage(imageFile);
    }
    return null;
  }

  public static boolean exec(final String command) {
    try {
      Process myProcess = Runtime.getRuntime().exec(command);
      myProcess.waitFor();
      return true;
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }
  }

  public Phone loadPhone(String phoneName) {
    final ClassLoader classLoader = getClass().getClassLoader();
    final File file = new File(classLoader.getResource("phones/" + phoneName + ".json").getFile());
    final ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.readValue(file, Phone.class);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public Script loadScript(String scriptName) {
    final ClassLoader classLoader = getClass().getClassLoader();
    final File file = new File(classLoader.getResource("scripts/" + scriptName + ".json").getFile());
    final ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.readValue(file, Script.class);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public void snap() {
    getScreen(OutputImage);

    Date date = new Date();

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
    String yyyyMMdd = sdf.format(date);

    OutputImage.renameTo(new File(yyyyMMdd + ".png"));
  }

}
