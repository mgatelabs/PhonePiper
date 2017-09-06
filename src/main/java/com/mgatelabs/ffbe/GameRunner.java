package com.mgatelabs.ffbe;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgatelabs.ffbe.shared.*;
import com.mgatelabs.ffbe.shared.details.ComponentDefinition;
import com.mgatelabs.ffbe.shared.image.RawImageWrapper;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        int maxLoops = 1000;

        SecureRandom secureRandom = new SecureRandom();

        // Start with a Screen shot

        System.out.println("----------------------");
        System.out.println("Game Runner");
        System.out.println("----------------------");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        while (maxLoops > 0) {
            long startTime = System.nanoTime();
            RawImageWrapper bufferedImage = getScreen();
            long endTime = System.nanoTime();

            long dif = endTime - startTime;

            if (!bufferedImage.isReady()) {
              System.out.println();
              System.out.println("----------------------");
              System.out.println("Error: Image Failure: " + ((float) dif / 1000000000.0));
              System.out.println("----------------------");
              continue;
            }

            ActionSet set = sets.get(currentSetIndex);

            int waitTime = set.getWaitTime();

            System.out.println();
            System.out.println("----------------------");
            System.out.println("Running Set: " + set.getTitle() + " - " + sdf.format(new Date()) + " : " + ((float) dif / 1000000000.0));
            System.out.println("----------------------");

            for (GameAction action : set.getPossibleActions()) {
                System.out.print("Testing Action: " + action.getTitle() + " - ");
                if (action.validate(bufferedImage)) {
                    System.out.println(" Success");

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
                        case SKIP: {

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
                            System.out.println();
                            System.out.println("Remaining Loops: " + maxLoops);
                            System.out.println();
                            System.out.println();
                            maxLoops--;
                        }
                        break;
                    } else if (action.isRestart()) {
                        System.out.println("Restarting Action");
                        break; // Stop processing
                    }
                } else {
                    System.out.println(" Failed");
                }
            }

            try {
                System.out.println("Waiting: " + waitTime + " ms");
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

    public void saveScreen() {
        long startTime = System.nanoTime();
        AdbUtils.persistScreen();
        long endTime = System.nanoTime();

        long dif = endTime - startTime;

        System.out.println("Frame captured in: " + ((float) dif / 1000000000.0)+"s");
    }

    public static RawImageWrapper getScreen() {
        byte[] bytes = exec2("adb exec-out screencap");
        try {
            int w, h;

            if (bytes.length > 12) {
                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                w = byteBuffer.getInt();
                h = byteBuffer.getInt();
            } else {
                w = 0;
                h = 0;
            }

            RawImageWrapper rawImageReader = new RawImageWrapper(w, h, RawImageWrapper.ImageFormats.RGBA, 12, bytes);
            return rawImageReader;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void runComponent(ComponentDefinition componentDefinition, com.mgatelabs.ffbe.shared.details.ActionType type) {

        final SecureRandom random = new SecureRandom();

        switch (type) {
            case TAP: {

                int x = componentDefinition.getX();
                int y = componentDefinition.getY();

                x += random.nextInt(componentDefinition.getW());
                y += random.nextInt(componentDefinition.getH());

                exec("adb shell input tap " + x + " " + y);
            } break;
            case SWIPE_UP: {

            } break;
            case SWIPE_DOWN: {

            } break;
        }
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

    public static byte[] exec2(final String command) {
        try {
            Process myProcess = Runtime.getRuntime().exec(command);
            return repair(myProcess.getInputStream());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    static byte[] repair(InputStream bytesIn) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int value;
        int temp = 0;
        boolean useTemp = false;
        BufferedInputStream bufferedInputStream = new BufferedInputStream(bytesIn);
        while ((value = bufferedInputStream.read()) != -1) {
            if (useTemp && value == 0x0A) {
                baos.write(value);
                useTemp = false;
                continue;
            } else if (useTemp) {
                baos.write(temp);
                useTemp = false;
            }
            if (value == 0x0d) {
                useTemp = true;
                temp = value;
            } else {
                baos.write(value);
            }
        }
        baos.close();
        return baos.toByteArray();
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

        long startTime = System.nanoTime();
        byte[] bytes = exec2("adb exec-out screencap -p");
        long endTime = System.nanoTime();
        long dif = endTime - startTime;
        System.out.println("Time Taken: " + ((float) dif / 1000000000.0));

        //getScreen(OutputImage);

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String yyyyMMdd = sdf.format(date);

        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(yyyyMMdd + ".png");
            fileOutputStream.write(bytes);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        OutputImage.renameTo(new File(yyyyMMdd + ".png"));


    }

}
