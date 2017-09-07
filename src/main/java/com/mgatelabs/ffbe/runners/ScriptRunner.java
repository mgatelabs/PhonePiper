package com.mgatelabs.ffbe.runners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mgatelabs.ffbe.shared.AdbUtils;
import com.mgatelabs.ffbe.shared.ColorSample;
import com.mgatelabs.ffbe.shared.ConsoleInput;
import com.mgatelabs.ffbe.shared.details.*;
import com.mgatelabs.ffbe.shared.helper.HelperUtils;
import com.mgatelabs.ffbe.shared.image.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017.
 */
public class ScriptRunner {

    private PlayerDetail playerDetail;
    private ScriptDetail scriptDetail;
    private DeviceDefinition deviceDefinition;
    private ViewDefinition viewDefinition;

    private ComponentDefinition energyBar;

    private Map<String, ScreenDefinition> screens;
    private Map<String, ComponentDefinition> components;

    private Map<String, StateTransfer> transferStateMap;

    private Stack<String> stack;

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String phoneIp;

    private HelperUtils helperUtils;

    private Set<String> validScreenIds;

    public ScriptRunner(PlayerDetail playerDetail, ScriptDetail scriptDetail, DeviceDefinition deviceDefinition, ViewDefinition viewDefinition) {
        this.playerDetail = playerDetail;
        this.scriptDetail = scriptDetail;
        this.deviceDefinition = deviceDefinition;
        this.viewDefinition = viewDefinition;
        stack = new Stack<>();

        screens = Maps.newHashMap();
        for (ScreenDefinition screenDefinition : viewDefinition.getScreens()) {
            screens.put(screenDefinition.getScreenId(), screenDefinition);
        }

        components = Maps.newHashMap();
        for (ComponentDefinition componentDefinition : viewDefinition.getComponents()) {
            components.put(componentDefinition.getComponentId(), componentDefinition);
        }

        energyBar = components.get("menu-energy_bar");
        if (energyBar == null) {
            throw new RuntimeException("Cannot find required component id: " + "menu-energy_bar");
        }

        for (Map.Entry<String, StateDetail> entry: scriptDetail.getStates().entrySet()) {
            entry.getValue().setId(entry.getKey());
        }

        System.out.println("------------");
        System.out.println("Generating State Info : " + getDateString());
        System.out.println("------------");
        System.out.println();

        transferStateMap = Maps.newHashMap();
        transferStateMap.putAll(generateStateInfo());

        validScreenIds = null;

        System.out.println("Use Phone Helper? (Y/N)");
        if (ConsoleInput.yesNo()) {
            System.out.println("Phone IP Address: " + (playerDetail.getIp() != null ? playerDetail.getIp() : "?"));
            phoneIp = ConsoleInput.getString();
            if (phoneIp.length() > 0) {
                playerDetail.setIp(phoneIp);
                if (playerDetail.write()) {
                    System.out.println("Player profile updated!");
                }
            } else if (phoneIp.length() == 0) {
                phoneIp = playerDetail.getIp();
            }

            helperUtils = new HelperUtils(phoneIp);

            InfoTransfer infoTransfer = new InfoTransfer();
            infoTransfer.setStates(transferStateMap);

            if (helperUtils.setup(infoTransfer)) {
                System.out.println("Image Server Ready!");
                System.out.println("Continue? (Y/N)");
                if (!ConsoleInput.yesNo()) {
                    return;
                }
            }

        } else {
            phoneIp = null;
            helperUtils = null;
        }
    }

    public static String getDateString() {
        return sdf.format(new Date());
    }

    private static Comparator<PointTransfer> pointTransferComparator = new Comparator<PointTransfer>() {
        @Override
        public int compare(PointTransfer o1, PointTransfer o2) {
            final int c = Integer.compare(o1.getOffset(), o2.getOffset());
            return c == 0 ? Integer.compare(o1.getIndex(), o2.getIndex()) : c;
        }
    };

    private Map<String, StateTransfer> generateStateInfo() {

        Map<String, StateTransfer> results = Maps.newHashMap();

        for (Map.Entry<String, StateDetail> stateEntry : scriptDetail.getStates().entrySet()) {

            StateTransfer stateTransfer = new StateTransfer();
            stateTransfer.setStateId(stateEntry.getKey());
            stateTransfer.setScreenIds(stateEntry.getValue().determineScreenIds());

            // Collect every point
            List<PointTransfer> points = Lists.newArrayList();
            for (int i = 0; i < stateTransfer.getScreenIds().size(); i++) {
                ScreenDefinition screenDefinition = screens.get(stateTransfer.getScreenIds().get(i));
                for (SamplePoint point : screenDefinition.getPoints()) {
                    points.add(new PointTransfer(RawImageWrapper.getOffsetFor(deviceDefinition.getWidth(), 12, point.getX(), point.getY(), RawImageWrapper.ImageFormats.RGBA), (byte) i, (byte) point.getR(), (byte) point.getG(), (byte) point.getB()));
                }
            }
            Collections.sort(points, pointTransferComparator);
            // Sort them
            stateTransfer.setPoints(points);
            int jumpOffset = 0;
            int readOffset = 0;
            for (PointTransfer transfer : points) {
                int requestedOffset = transfer.getOffset();
                if (requestedOffset == jumpOffset) {
                    transfer.setOffset(0);
                } else {
                    jumpOffset = requestedOffset;
                    transfer.setOffset(jumpOffset - readOffset);
                    readOffset = jumpOffset + 3; // Read ahead for 3 bytes
                }
            }

            results.put(stateTransfer.getStateId(), stateTransfer);
        }

        return results;
    }

    public void run(String stateName) {

        ImageWrapper imageWrapper;

        StateDetail stateDetail = scriptDetail.getStates().get(stateName);

        if (stateDetail == null) {
            throw new RuntimeException("Cannot find state with id: " + stateName);
        }

        while (true) {

            if (helperUtils != null && helperUtils.getFailures() > 20) {
                helperUtils = null;
            }

            if (helperUtils != null) {
                long startTime = System.nanoTime();
                AdbUtils.persistScreen();
                long endTime = System.nanoTime();

                long dif = endTime - startTime;

                String seconds = String.format("%2.2f", ((float) dif / 1000000000.0));

                System.out.println("------------");
                System.out.println("Image: Persisted " + seconds + "s" + " : " + getDateString());
                System.out.println("------------");
                System.out.println();

                imageWrapper = null;
            } else {

                long startTime = System.nanoTime();
                imageWrapper = AdbUtils.getScreen();
                long endTime = System.nanoTime();

                long dif = endTime - startTime;

                String seconds = String.format("%2.2f", ((float) dif / 1000000000.0));

                if (imageWrapper == null || !imageWrapper.isReady()) {
                    System.out.println("------------");
                    System.out.println("Image: Failure: " + seconds + "s" + " : " + getDateString());
                    System.out.println("------------");
                    System.out.println();
                    waitFor(250);
                    continue;
                } else {
                    System.out.println("------------");
                    System.out.println("Image: Success " + seconds + "s" + " : " + getDateString());
                    System.out.println("------------");
                    System.out.println();
                }
            }

            boolean keepRunning = true;

            while (keepRunning) {
                keepRunning = false;

                if (helperUtils != null) {

                    System.out.println("------------");
                    System.out.println("Helper: /check/" + stateDetail.getId() + " : " + getDateString());
                    System.out.println("------------");
                    System.out.println();

                    validScreenIds = helperUtils.check(stateDetail.getId());
                }

                StateResult result = state(stateDetail, imageWrapper);

                switch (result.getType()) {
                    case STOP: {
                        return;
                    }
                    case POP:
                        if (stack.size() > 0) {
                            final String oldState = stack.pop();
                            stateDetail = scriptDetail.getStates().get(oldState);
                            if (stateDetail == null) {
                                throw new RuntimeException("Cannot find state with id: " + oldState);
                            }
                            keepRunning = false;
                        } else {
                            throw new RuntimeException("Stack is empty");
                        }
                        break;
                    case MOVE: {
                        stateDetail = scriptDetail.getStates().get(result.getValue());
                        if (stateDetail == null) {
                            throw new RuntimeException("Cannot find state with id: " + result.getValue());
                        }
                    }
                    break;
                    case PUSH: {
                        stack.push(stateDetail.getName());
                        stateDetail = scriptDetail.getStates().get(result.getValue());
                        if (stateDetail == null) {
                            throw new RuntimeException("Cannot find state with id: " + result.getValue());
                        }
                        keepRunning = false;
                    }
                    break;
                    case SWAP: {
                        stateDetail = scriptDetail.getStates().get(result.getValue());
                        if (stateDetail == null) {
                            throw new RuntimeException("Cannot find state with id: " + result.getValue());
                        }
                        keepRunning = true;
                    }
                    break;
                    case REPEAT: {
                        keepRunning = false;
                    }
                    break;
                }

            }

            waitFor(1000);
        }

    }

    private StateResult state(final StateDetail stateDetail, final ImageWrapper imageWrapper) {

        System.out.println("------------");
        System.out.println("Running State: " + stateDetail.getName() + " : " + getDateString());
        System.out.println("------------");
        System.out.println();

        for (StatementDefinition statementDefinition : stateDetail.getStatements()) {
            if (check(statementDefinition.getCondition(), imageWrapper)) {
                for (ActionDefinition actionDefinition : statementDefinition.getActions()) {
                    switch (actionDefinition.getType()) {
                        case MSG: {
                            System.out.println("------------");
                            System.out.println("MSG: " + actionDefinition.getValue() + " : " + getDateString());
                            System.out.println("------------");
                            System.out.println();
                        }
                        break;
                        case TAP: {
                            ComponentDefinition componentDefinition = components.get(actionDefinition.getValue());
                            if (componentDefinition == null) {
                                throw new RuntimeException("Cannot find component with id: " + actionDefinition.getValue());
                            }
                            AdbUtils.component(componentDefinition, ActionType.TAP);
                        }
                        break;
                        case WAIT: {
                            long time = Long.parseLong(actionDefinition.getValue());
                            if (time > 0) {
                                waitFor(time);
                            } else {
                                throw new RuntimeException("Invalid wait time: " + actionDefinition.getValue());
                            }
                        }
                        break;
                        case POP: {
                            return StateResult.POP;
                        }
                        case PUSH: {
                            return StateResult.push(actionDefinition.getValue());
                        }
                        case SWAP: {
                            return StateResult.move(actionDefinition.getValue());
                        }
                        case MOVE: {
                            return StateResult.swap(actionDefinition.getValue());
                        }
                        case REPEAT: {
                            return StateResult.REPEAT;
                        }
                    }
                }
            }
        }

        return StateResult.REPEAT;
    }

    private void waitFor(long milli) {
        try {
            Thread.sleep(milli);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean check(final ConditionDefinition conditionDefinition, ImageWrapper imageWrapper) {

        boolean result = false;
        boolean checkAnd = true;

        switch (conditionDefinition.getIs()) {
            case BOOLEAN: {
                result = "true".equalsIgnoreCase(conditionDefinition.getValue());
            }
            break;
            case SCREEN: {
                if (helperUtils != null) {
                    result = validScreenIds.contains(conditionDefinition.getValue());
                } else {
                    ScreenDefinition screenDefinition = screens.get(conditionDefinition.getValue());
                    if (screenDefinition == null) {
                        throw new RuntimeException("Cannot find screen with id: " + conditionDefinition.getValue());
                    }
                    result = SamplePoint.validate(screenDefinition.getPoints(), imageWrapper);
                }
            }
            break;
            case ENERGY: {
                int energy = Integer.parseInt(conditionDefinition.getValue());
                if (energy >= PlayerDetail.MIN_ENERGY && energy <= PlayerDetail.MAX_ENERGY) {
                    float requiredPercent = ((float) energy / (float) playerDetail.getTotalEnergy());
                    int requiredPixel = ((int) (energyBar.getW() * requiredPercent) + 1);
                    if (requiredPixel > energyBar.getW()) {
                        requiredPixel = energyBar.getW();
                    }
                    ColorSample sample = new ColorSample();
                    if (helperUtils != null) {
                        int [] pixels = helperUtils.pixel(RawImageWrapper.getOffsetFor(deviceDefinition.getWidth(), 12, energyBar.getX(), energyBar.getY(), RawImageWrapper.ImageFormats.RGBA));
                        if (pixels != null) {
                            sample.setR(pixels[0]);
                            sample.setG(pixels[1]);
                            sample.setB(pixels[2]);
                        } else {

                        }
                    } else {
                        imageWrapper.getPixel(energyBar.getX() + requiredPixel, energyBar.getY(), sample);
                    }
                    result = sample.getB() > 100;
                } else {
                    throw new RuntimeException("Invalid energy value: " + conditionDefinition.getValue());
                }
            }
            break;
            case NOT: {
                if (conditionDefinition.getAnd() == null) {
                    throw new RuntimeException("is (NOT) requires a AND value");
                }
                checkAnd = false;
                result = !check(conditionDefinition.getAnd(), imageWrapper);
            }
            break;
        }

        // If we have a AND handle it
        if (result && checkAnd && conditionDefinition.getAnd() != null) {
            result = check(conditionDefinition.getAnd(), imageWrapper);
        }

        // If we failed, but have a OR, check the OR
        if (!result && conditionDefinition.getOr() != null) {
            result = check(conditionDefinition.getOr(), imageWrapper);
        }

        return result;
    }
}
