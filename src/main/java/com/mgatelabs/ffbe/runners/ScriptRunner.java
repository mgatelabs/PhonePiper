package com.mgatelabs.ffbe.runners;

import com.google.common.collect.Maps;
import com.mgatelabs.ffbe.shared.AdbUtils;
import com.mgatelabs.ffbe.shared.ColorSample;
import com.mgatelabs.ffbe.shared.SamplePoint;
import com.mgatelabs.ffbe.shared.details.*;
import com.mgatelabs.ffbe.shared.image.ImageWrapper;

import java.text.*;
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

    private Stack<String> stack;

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
    }

    public static String getDateString() {
        return sdf.format(new Date());
    }

    public void run(String stateName) {

        ImageWrapper imageWrapper;

        StateDetail stateDetail = scriptDetail.getStates().get(stateName);

        if (stateDetail == null) {
            throw new RuntimeException("Cannot find state with id: " + stateName);
        }

        while (true) {
            long startTime = System.nanoTime();
            imageWrapper = AdbUtils.screen();
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

            boolean keepRunning = true;

            while (keepRunning) {
                keepRunning = false;

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

            waitFor(500);
        }

    }

    private StateResult state(final StateDetail stateDetail, final ImageWrapper imageWrapper) {

        System.out.println("------------");
        System.out.println("Running State: " + stateDetail.getName()+ " : " + getDateString());
        System.out.println("------------");
        System.out.println();

        for (StatementDefinition statementDefinition : stateDetail.getStatements()) {
            if (check(statementDefinition.getCondition(), imageWrapper)) {
                for (ActionDefinition actionDefinition : statementDefinition.getActions()) {
                    switch (actionDefinition.getType()) {
                        case MSG: {
                            System.out.println("------------");
                            System.out.println("MSG: " + actionDefinition.getValue()+ " : " + getDateString());
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
                ScreenDefinition screenDefinition = screens.get(conditionDefinition.getValue());
                if (screenDefinition == null) {
                    throw new RuntimeException("Cannot find screen with id: " + conditionDefinition.getValue());
                }
                result = SamplePoint.validate(screenDefinition.getPoints(), imageWrapper);
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
                    imageWrapper.getPixel(energyBar.getX() + requiredPixel, energyBar.getY(), sample);
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
