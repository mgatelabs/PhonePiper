package com.mgatelabs.piper.runners;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mgatelabs.piper.shared.details.*;
import com.mgatelabs.piper.shared.helper.DeviceHelper;
import com.mgatelabs.piper.shared.helper.InfoTransfer;
import com.mgatelabs.piper.shared.helper.MapTransfer;
import com.mgatelabs.piper.shared.helper.PointTransfer;
import com.mgatelabs.piper.shared.image.*;
import com.mgatelabs.piper.shared.util.AdbShell;
import com.mgatelabs.piper.shared.util.AdbUtils;
import com.mgatelabs.piper.shared.util.VarTimer;
import com.mgatelabs.piper.ui.utils.WebLogHandler;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017.
 */
public class ScriptRunner {

    public enum Status {
        INIT,
        READY,
        RUNNING,
        PAUSED,
        STOPPED
    }

    private PlayerDefinition playerDefinition;
    private ConnectionDefinition connectionDefinition;
    private ScriptDefinition scriptDefinition;
    private DeviceDefinition deviceDefinition;
    private ViewDefinition viewDefinition;

    private ComponentDefinition energyBar;

    private Map<String, ScreenDefinition> screens;
    private Map<String, ComponentDefinition> components;

    private Map<String, StateTransfer> transferStateMap;
    private MapTransfer transferMap;

    private Stack<String> stack;

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private DeviceHelper deviceHelper;

    private Set<String> validScreenIds;

    private AdbShell shell;

    private Map<String, Integer> vars;

    private volatile Status status;

    private Date lastImageDate;
    private float lastImageDuration;

    private Logger logger = Logger.getLogger("ScriptRunner");

    private Map<String, VarTimer> timers;

    //private static final String VAR_SECONDS = "_seconds";
    private static final String VAR_LOOPS = "_loops";

    private long elapsedTime;

    public ScriptRunner(PlayerDefinition playerDefinition, ConnectionDefinition connectionDefinition, DeviceHelper deviceHelper, ScriptDefinition scriptDefinition, DeviceDefinition deviceDefinition, ViewDefinition viewDefinition, WebLogHandler webLogHandler, Handler fileHandler) {
        this.playerDefinition = playerDefinition;
        this.scriptDefinition = scriptDefinition;
        this.deviceDefinition = deviceDefinition;
        this.connectionDefinition = connectionDefinition;
        this.viewDefinition = viewDefinition;
        timers = Maps.newHashMap();
        stack = new Stack<>();
        vars = Maps.newHashMap();
        shell = new AdbShell(deviceDefinition);
        shell.attachhandler(webLogHandler);


        logger.removeHandler(webLogHandler);
        logger.addHandler(webLogHandler);
        if (fileHandler != null) {
            shell.attachhandler(fileHandler);
            logger.removeHandler(fileHandler);
            logger.addHandler(fileHandler);
        }
        Level min = webLogHandler.getLevel();
        if (fileHandler != null) {
            min = webLogHandler.getLevel().intValue() < fileHandler.getLevel().intValue() ? webLogHandler.getLevel(): fileHandler.getLevel();
        }
        logger.setLevel(webLogHandler.getLevel());
        shell.setLevel(webLogHandler.getLevel());


        logger.finer("Extracting Variables");

        elapsedTime = 0;

        // Script Includes
        for (String scriptIncludeId : scriptDefinition.getIncludes()) {
            ScriptDefinition otherDefinition = ScriptDefinition.read(scriptIncludeId);
            if (otherDefinition == null) {
                logger.log(Level.SEVERE, "Could not find Script include: " + scriptIncludeId);
            } else {
                // Add Replace any existing state with states from the includes
                for (Map.Entry<String, StateDefinition> otherState : otherDefinition.getStates().entrySet()) {
                    if (scriptDefinition.getStates().containsKey(otherState.getKey())) {
                        continue;
                    }
                    scriptDefinition.getStates().put(otherState.getKey(), otherState.getValue());
                }
                // Add all vars
                for (VarDefinition varDefinition : otherDefinition.getVars()) {
                    boolean exists = false;
                    for (VarDefinition currentDef : scriptDefinition.getVars()) {
                        if (currentDef.getName().equals(varDefinition.getName())) {
                            exists = true;
                            break;
                        }

                    }
                    if (!exists) {
                        scriptDefinition.getVars().add(varDefinition);
                    }
                }
            }
        }

        for (StateDefinition stateDefinition : scriptDefinition.getStates().values()) {
            if (!stateDefinition.getIncludes().isEmpty()) {
                for (String includeName : stateDefinition.getIncludes()) {
                    if (scriptDefinition.getStates().containsKey(includeName)) {
                        stateDefinition.getStatements().addAll(scriptDefinition.getStates().get(includeName).getStatements());
                    } else {
                        logger.log(Level.SEVERE, "Could not find statement include: " + includeName);
                    }

                }
            }
        }

        //scriptDefinition.getVars().add(new VarDefinition(VAR_SECONDS, "Elapsed Seconds", "0", VarType.INT, VarDisplay.SECONDS, VarModify.VISIBLE));
        //scriptDefinition.getVars().add(new VarDefinition(VAR_LOOPS, "Loops", "0", VarType.INT, VarDisplay.STANDARD, VarModify.HIDDEN));

        for (VarDefinition varDefinition : scriptDefinition.getVars()) {
            if (varDefinition.getType() == VarType.INT) {
                addVar(varDefinition.getName(), Integer.parseInt(varDefinition.getValue()));
            } else if (varDefinition.getType() == VarType.TIMER) {
                addVar(varDefinition.getName(), Integer.parseInt(varDefinition.getValue()));
                timers.put(varDefinition.getName(), new VarTimer(false));
            }
        }

        scriptDefinition.getVars().sort(new Comparator<VarDefinition>() {
            @Override
            public int compare(VarDefinition o1, VarDefinition o2) {
                if (o1.getOrder() == o2.getOrder()) {
                    return o1.getName().compareTo(o2.getName());
                }
                return Integer.compare(o1.getOrder(), o2.getOrder());
            }
        });

        logger.finer("Extracting Screens");

        screens = Maps.newHashMap();
        for (ScreenDefinition screenDefinition : viewDefinition.getScreens()) {
            screens.put(screenDefinition.getScreenId(), screenDefinition);
        }

        logger.finer("Extracting Components");

        components = Maps.newHashMap();
        for (ComponentDefinition componentDefinition : viewDefinition.getComponents()) {
            components.put(componentDefinition.getComponentId(), componentDefinition);
        }

        energyBar = components.get("menu-energy_bar");
        if (energyBar == null) {
            logger.log(Level.SEVERE, "Cannot find required component id: " + "menu-energy_bar");
            throw new RuntimeException("Cannot find required component id: " + "menu-energy_bar");
        }

        for (Map.Entry<String, StateDefinition> entry : scriptDefinition.getStates().entrySet()) {
            entry.getValue().setId(entry.getKey());
        }

        logger.finer("Generating State Info");

        transferStateMap = Maps.newHashMap();
        transferStateMap.putAll(generateStateInfo());

        transferMap = new MapTransfer();
        ComponentDefinition miniMapArea = components.get("dungeon-mini_map-area");
        ComponentDefinition miniMapAreaCenter = components.get("dungeon-mini_map-center");
        transferMap.setup(deviceDefinition.getWidth(), 12, 4, miniMapArea.getW(), miniMapArea.getH(), miniMapArea.getX(), miniMapArea.getY(), miniMapAreaCenter.getW(), miniMapAreaCenter.getH());

        validScreenIds = null;

        this.deviceHelper = deviceHelper;

        status = Status.INIT;
    }

    public void updateLogger(Level level) {
        logger.setLevel(level);
        shell.setLevel(level);
    }

    public void stopShell() {
        if (shell != null) {
            try {
                shell.shutdown();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            shell = null;
        }
    }

    public void restartShell() {
        if (shell != null) {
            try {
                shell.shutdown();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        shell = new AdbShell(deviceDefinition);
        updateLogger(logger.getLevel());
    }

    public Date getLastImageDate() {
        return lastImageDate;
    }

    public float getLastImageDuration() {
        return lastImageDuration;
    }

    public boolean initHelper() {
        if (deviceHelper == null) {
            logger.log(Level.SEVERE, "Phone Helper connection is down, please restart app");
            return false;
        }
        if (deviceHelper.ready()) {
            logger.log(Level.SEVERE, "Phone Helper is ready @ " + deviceHelper.getIpAddress());
            InfoTransfer infoTransfer = new InfoTransfer();
            infoTransfer.setStates(transferStateMap);
            infoTransfer.setMap(transferMap);
            if (deviceHelper.setup(infoTransfer)) {
                logger.log(Level.SEVERE, "Phone Helper is configured");
                return true;
            } else {
                logger.log(Level.SEVERE, "Phone Helper is not configured");
            }
        } else {
            logger.log(Level.SEVERE, "Phone Helper is not configured");
        }
        return false;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    private int getVar(String name) {
        return vars.getOrDefault(name, 0);
    }

    private void addVar(String name, int value) {
        vars.put(name, getVar(name) + value);
    }

    private void setVar(String name, int value) {
        final VarDefinition varDefinition = getVarDefinition(name);
        switch (varDefinition.getType()) {
            case TIMER: {
                if (value == 0) {
                    VarTimer timer = timers.get(varDefinition.getName());
                    timer.reset();
                    vars.put(name, value);
                }
            }
            break;
            case INT: {
                vars.put(name, value);
            }
            break;
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

        for (Map.Entry<String, StateDefinition> stateEntry : scriptDefinition.getStates().entrySet()) {

            // Skip all include states
            if (stateEntry.getKey().startsWith("_")) continue;

            StateTransfer stateTransfer = new StateTransfer();
            stateTransfer.setStateId(stateEntry.getKey());
            List<String> determinedScreenIds = Lists.newArrayList();

            List<String> tempScreenIds = stateEntry.getValue().determineScreenIds();
            for (String screeId : tempScreenIds) {
                ScreenDefinition screenDefinition = screens.get(screeId);
                if (screenDefinition == null) {
                    System.out.println("Unknown Screen Id: " + screeId);
                    logger.log(Level.SEVERE, "Unknown Screen Id: " + screeId);
                    continue;
                } else if (screenDefinition.getPoints() == null) {
                    System.out.println("Bad Screen Id: " + screeId);
                    logger.log(Level.SEVERE, "Bad Screen Id: " + screeId);
                    continue;
                }
                if (!screenDefinition.isEnabled() || screenDefinition.getPoints() == null || (screenDefinition.getPoints() != null && screenDefinition.getPoints().isEmpty())) {
                    logger.log(Level.SEVERE, "Disabled Screen: " + screenDefinition.getScreenId() + " for state: " + stateEntry.getValue().getId());
                    continue;
                }
                boolean valid = true;
                for (SamplePoint point : screenDefinition.getPoints()) {
                    if (point.getX() > deviceDefinition.getWidth() || point.getY() >= deviceDefinition.getHeight()) {
                        valid = false;
                        logger.log(Level.SEVERE, "Invalid Screen Point for Screen: " + screenDefinition.getScreenId());
                        break;
                    }
                }
                if (valid) {
                    determinedScreenIds.add(screeId);
                }
            }
            stateTransfer.setScreenIds(determinedScreenIds);

            // Collect every point
            List<PointTransfer> points = Lists.newArrayList();
            for (int i = 0; i < stateTransfer.getScreenIds().size(); i++) {
                ScreenDefinition screenDefinition = screens.get(stateTransfer.getScreenIds().get(i));
                if (!screenDefinition.isEnabled()) continue;
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

    public boolean isRunning() {
        return this.status == Status.RUNNING;
    }

    public boolean isWorking() {
        return this.status == Status.RUNNING || this.status == Status.STOPPED;
    }

    public boolean isStopping() {
        return this.status == Status.PAUSED;
    }

    public boolean isStopped() {
        return this.status == Status.STOPPED;
    }

    private String currentStateId;

    public String getCurrentStateId() {
        return currentStateId;
    }

    public Set<String> getValidScreenIds() {
        return ImmutableSet.copyOf(validScreenIds);
    }

    public void run(String stateName) {
        try {
            currentStateId = stateName;
            this.status = Status.RUNNING;

            logger.log(Level.FINE, "Init Helper");
            initHelper();

            ImageWrapper imageWrapper;

            StateDefinition stateDefinition = scriptDefinition.getStates().get(stateName);

            if (stateDefinition == null) {
                logger.log(Level.SEVERE, "Cannot find state with id: " + stateName);
                setStatus(Status.INIT);
                throw new RuntimeException("Cannot find state with id: " + stateName);
            } else {
                logger.log(Level.FINE, "Found initial state with id: " + stateName);
            }

            // Always reset the loop counter
            setVar(VAR_LOOPS, 0);

            while (isRunning()) {

                for (VarDefinition varDefinition : getRawEditVariables()) {
                    if (varDefinition.getType() == VarType.TIMER) {
                        final VarTimer timer = timers.get(varDefinition.getName());
                        timer.forward();
                        vars.put(varDefinition.getName(), (int) TimeUnit.NANOSECONDS.toSeconds(timer.getElapsed()));
                    }
                }

                if (!shell.isReady()) {
                    logger.log(Level.WARNING, "Bad Shell: Wait, Connect, Restart, Wait...");
                    if (connectionDefinition.isWifi()) {
                        Thread.sleep(1000);
                        AdbShell.connect(deviceHelper.getIpAddress());
                    }
                    Thread.sleep(1000);
                    restartShell();
                    Thread.sleep(1000);
                }

                if (deviceHelper != null) {
                    logger.finest("Helper Image");
                    long startTime = System.nanoTime();
                    AdbUtils.persistScreen(shell);
                    long endTime = System.nanoTime();

                    long dif = endTime - startTime;

                    lastImageDate = new Date();
                    lastImageDuration = ((float) dif / 1000000000.0f);

                    logger.finest("Image Persisted in " + lastImageDuration);

                    imageWrapper = null;
                } else {
                    logger.finest("USB Image");

                    long startTime = System.nanoTime();
                    imageWrapper = AdbUtils.getScreen();
                    long endTime = System.nanoTime();

                    long dif = endTime - startTime;

                    lastImageDate = new Date();
                    lastImageDuration = ((float) dif / 1000000000.0f);

                    if (imageWrapper == null || !imageWrapper.isReady()) {
                        logger.warning("Image Failure");
                        waitFor(250);
                        continue;
                    } else {
                        logger.finest("Image Persisted in " + lastImageDuration);
                    }
                }

                boolean keepRunning = true;

                while (keepRunning && isRunning()) {
                    keepRunning = false;

                    if (deviceHelper != null) {

                        logger.finer("Helper: /check/" + stateDefinition.getId());

                        validScreenIds = deviceHelper.check(stateDefinition.getId());

                        if (logger.getLevel() == Level.FINEST) {
                            logger.log(Level.FINEST, "Valid Screens: " + Joiner.on(",").join(validScreenIds));
                        }
                    }

                    StateResult result = state(stateDefinition, imageWrapper);

                    switch (result.getType()) {
                        case STOP: {
                            this.status = Status.STOPPED;
                            return;
                        }
                        case POP:
                            if (stack.size() > 0) {
                                final String oldState = stack.pop();
                                stateDefinition = scriptDefinition.getStates().get(oldState);
                                if (stateDefinition == null) {
                                    logger.log(Level.SEVERE, "Cannot find state with id: " + oldState);
                                    throw new RuntimeException("Cannot find state with id: " + oldState);
                                }
                                keepRunning = false;
                            } else {
                                throw new RuntimeException("Stack is empty");
                            }
                            break;
                        case MOVE: {
                            stateDefinition = scriptDefinition.getStates().get(result.getValue());
                            if (stateDefinition == null) {
                                logger.log(Level.SEVERE, "Cannot find state with id: " + result.getValue());
                                throw new RuntimeException("Cannot find state with id: " + result.getValue());
                            }
                            currentStateId = stateDefinition.getId();
                            keepRunning = false;
                        }
                        break;
                        case PUSH: {
                            stack.push(stateDefinition.getId());
                            stateDefinition = scriptDefinition.getStates().get(result.getValue());
                            if (stateDefinition == null) {
                                logger.log(Level.SEVERE, "Cannot find state with id: " + result.getValue());
                                throw new RuntimeException("Cannot find state with id: " + result.getValue());
                            }
                            currentStateId = stateDefinition.getId();
                            keepRunning = true;
                        }
                        break;
                        case SWAP: {
                            stateDefinition = scriptDefinition.getStates().get(result.getValue());
                            if (stateDefinition == null) {
                                logger.log(Level.SEVERE, "Cannot find state with id: " + result.getValue());
                                throw new RuntimeException("Cannot find state with id: " + result.getValue());
                            }
                            currentStateId = stateDefinition.getId();
                            keepRunning = true;
                        }
                        break;
                        case REPEAT: {
                            keepRunning = false;
                        }
                        break;
                    }

                }

                waitFor(250);
            }

        } catch (Exception ex) {
          logger.log(Level.SEVERE, ex.getMessage());
          ex.printStackTrace();
        } finally {
            setStatus(Status.STOPPED);
            logger.info("Script Stopped");
        }
    }

    private String lapEvent(String id) {
        VarTimer timer = timers.get(id);
        if (timer == null) {
            timer = new VarTimer(true);
            timers.put(id, timer);
        }
        timer.time();
        return "Lap: " + id + " : " + timer.toString();
    }

    private int valueHandler(String value) {
        if (value.startsWith("$")) {
            return getVar(value.substring(1));
        } else {
            return Integer.parseInt(value);
        }
    }

    private String valueHandlerAsString(String value) {
        if (value.startsWith("$")) {
            return Integer.toString(getVar(value.substring(1)));
        } else {
            return Integer.toString(Integer.parseInt(value));
        }
    }

    private boolean stillRunning() {
        return status == Status.RUNNING;
    }

    private StateResult state(final StateDefinition stateDefinition, final ImageWrapper imageWrapper) {

        logger.fine("Running State: " + stateDefinition.getName());

        boolean batchCmds = false;

        for (StatementDefinition statementDefinition : stateDefinition.getStatements()) {
            if (check(statementDefinition.getCondition(), imageWrapper)) {
                for (ActionDefinition actionDefinition : statementDefinition.getActions()) {
                    final int loopIndex = actionDefinition.getCount() <= 0 ? 1 : actionDefinition.getCount();
                    for (int looper = 0; looper < loopIndex; looper++) {
                        if (!stillRunning()) {
                            return StateResult.STOP;
                        }
                        switch (actionDefinition.getType()) {
                            case MSG: {
                                String msg = actionDefinition.getValue();
                                int startindex;
                                while ((startindex = msg.indexOf("${")) >= 0) {
                                    int endIndex = msg.indexOf('}', startindex);
                                    if (endIndex > startindex + 2) {
                                        String varName = msg.substring(startindex += 2, endIndex).trim();
                                        if (varName.length() > 0) {
                                            if (vars.containsKey(varName)) {
                                                msg = msg.substring(0, startindex - 2) + vars.get(varName) + msg.substring(endIndex + 1);
                                            } else {
                                                break;
                                            }
                                        }
                                    } else {
                                        break;
                                    }
                                }
                                logger.info("MSG: " + msg);
                            }
                            break;
                            case BATCH: {
                                if ("START".equalsIgnoreCase(actionDefinition.getValue())) {
                                    batchCmds = true;
                                } else if (batchCmds) {
                                    batchCmds = false;
                                    shell.exec();
                                }
                            }
                            break;
                            case SET: {
                                String varName = actionDefinition.getVar();
                                int value = valueHandler(actionDefinition.getValue());
                                setVar(varName, value);
                            }
                            break;
                            case LAP: {
                                String timerId = actionDefinition.getValue();
                                timerId = (timerId == null || timerId.trim().length() == 0) ? "generic" : timerId.trim();
                                logger.info(lapEvent(timerId));
                            }
                            break;
                            case ADD: {
                                String varName = actionDefinition.getVar();
                                int value = valueHandler(actionDefinition.getValue());
                                addVar(varName, value);
                            }
                            break;
                            case TAP:
                            case SWIPE_DOWN:
                            case SLOW_DOWN:
                            case SLOW_LEFT:
                            case SLOW_RIGHT:
                            case SWIPE_UP:
                            case SLOW_UP:
                            case SWIPE_LEFT:
                            case SWIPE_RIGHT: {
                                ComponentDefinition componentDefinition = components.get(actionDefinition.getValue());
                                if (componentDefinition == null) {
                                    logger.log(Level.SEVERE, "Cannot find component with id: " + actionDefinition.getValue());
                                    throw new RuntimeException("Cannot find component with id: " + actionDefinition.getValue());
                                }
                                AdbUtils.component(deviceDefinition, componentDefinition, actionDefinition.getType(), shell, batchCmds);
                            }
                            break;
                            case EVENT: {
                                if (!AdbUtils.event(actionDefinition.getValue(), shell, batchCmds)) {
                                    logger.log(Level.SEVERE, "Unknown event id: " + actionDefinition.getValue());
                                    throw new RuntimeException("Unknown event id: " + actionDefinition.getValue());
                                }
                            }
                            break;
                            case INPUT: {
                                if (!AdbUtils.event(valueHandlerAsString(actionDefinition.getValue()), shell, batchCmds)) {
                                    logger.log(Level.SEVERE, "Unknown event id: " + actionDefinition.getValue());
                                    throw new RuntimeException("Unknown event id: " + actionDefinition.getValue());
                                }
                            }
                            break;
                            case WAIT: {
                                int time = valueHandler(actionDefinition.getValue());
                                if (time > 0) {
                                    waitFor(time);
                                } else {
                                    logger.log(Level.SEVERE, "Invalid wait time: " + actionDefinition.getValue());
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
                                return StateResult.swap(actionDefinition.getValue());
                            }
                            case MOVE: {
                                return StateResult.move(actionDefinition.getValue());
                            }
                            case REPEAT: {
                                return StateResult.REPEAT;
                            }
                            case STOP:
                                return StateResult.STOP;
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
        boolean failure = false;

        switch (conditionDefinition.getUsedCondition()) {
            case BOOLEAN: {
                result = "true".equalsIgnoreCase(conditionDefinition.getValue());
            }
            break;
            case GREATER:
            case LESS:
            case EQUAL: {
                int value;
                if (conditionDefinition.getValue().startsWith("$")) {
                    value = getVar(conditionDefinition.getValue().substring(1));
                } else {
                    value = Integer.parseInt(conditionDefinition.getValue());
                }
                String varName = conditionDefinition.getVar();
                int currentValue = getVar(varName);
                switch (conditionDefinition.getUsedCondition()) {
                    case GREATER: {
                        result = currentValue > value;
                    }
                    break;
                    case LESS: {
                        result = currentValue < value;
                    }
                    break;
                    case EQUAL: {
                        result = currentValue == value;
                    }
                    break;
                }
            }
            break;
            case SCREEN: {
                ScreenDefinition screenDefinition = screens.get(conditionDefinition.getValue());
                if (screenDefinition == null || !screenDefinition.isEnabled() || screenDefinition.getPoints() == null || screenDefinition.getPoints().isEmpty()) {
                    failure = true;
                } else {
                    if (deviceHelper != null) {
                        result = validScreenIds.contains(conditionDefinition.getValue());
                    } else {

                        if (screenDefinition == null) {
                            logger.log(Level.SEVERE, "Cannot find screen with id: " + conditionDefinition.getValue());
                            throw new RuntimeException("Cannot find screen with id: " + conditionDefinition.getValue());
                        }
                        result = SamplePoint.validate(screenDefinition.getPoints(), imageWrapper, false);
                    }
                }
            }
            break;
            case ENERGY: {
                int energy;
                if (conditionDefinition.getVar() != null && conditionDefinition.getVar().trim().length() > 0) {
                    energy = getVar(conditionDefinition.getVar());
                } else {
                    energy = Integer.parseInt(conditionDefinition.getValue());
                }
                if (energy >= PlayerDefinition.MIN_ENERGY && energy <= PlayerDefinition.MAX_ENERGY) {
                    float requiredPercent = ((float) energy / (float) playerDefinition.getTotalEnergy());
                    int requiredPixel = ((int) (energyBar.getW() * requiredPercent) + 1);
                    if (requiredPixel > energyBar.getW()) {
                        requiredPixel = energyBar.getW();
                    }
                    Sampler sample = new Sampler();
                    if (deviceHelper != null) {
                        int[] pixels = deviceHelper.pixel(RawImageWrapper.getOffsetFor(deviceDefinition.getWidth(), 12, energyBar.getX() + requiredPixel, energyBar.getY(), RawImageWrapper.ImageFormats.RGBA));
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
                    logger.log(Level.SEVERE, "Invalid energy value: " + conditionDefinition.getValue());
                    throw new RuntimeException("Invalid energy value: " + conditionDefinition.getValue());
                }
            }
            break;
        }

        if (conditionDefinition.isReversed()) {
            result = !result;
        }

        // If we have a AND handle it
        if (!failure && result && checkAnd && !conditionDefinition.getAnd().isEmpty()) {
            for (ConditionDefinition sub : conditionDefinition.getAnd()) {
                if (!check(sub, imageWrapper)) {
                    result = false;
                    break;
                }
            }
        }

        if (failure) {
            result = false;
        }

        // If we succeed, but have a ANDOR, check the ORs
        if (result && !conditionDefinition.getAndOr().isEmpty()) {
            result = false; // force failure
            for (ConditionDefinition sub : conditionDefinition.getAndOr()) {
                if (check(sub, imageWrapper)) {
                    result = true;
                    break;
                }
            }
        }

        // If we failed, but have a OR, check the OR
        if (!result && !conditionDefinition.getOr().isEmpty()) {
            for (ConditionDefinition sub : conditionDefinition.getOr()) {
                if (check(sub, imageWrapper)) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    public List<VarDefinition> getVariables() {
        List<VarDefinition> vars = Lists.newArrayList();
        for (VarDefinition varDefinition : scriptDefinition.getVars()) {
            switch (varDefinition.getModify()) {
                case HIDDEN:
                    continue;
                case VISIBLE:
                case EDITABLE: {
                    if (varDefinition.getType() == VarType.INT || varDefinition.getType() == VarType.TIMER) {
                        vars.add(new VarDefinition(varDefinition.getName(), varDefinition.getDisplay(), Integer.toString(getVar(varDefinition.getName())), varDefinition.getType(), varDefinition.getDisplayType(), varDefinition.getModify(), varDefinition.getOrder()));
                    }
                }
                break;
            }
        }
        return vars;
    }

    public List<VarDefinition> getRawEditVariables() {
        List<VarDefinition> vars = Lists.newArrayList();
        for (VarDefinition varDefinition : scriptDefinition.getVars()) {
            switch (varDefinition.getModify()) {
                case HIDDEN:
                    continue;
                case VISIBLE:
                case EDITABLE: {
                    if (varDefinition.getType() == VarType.INT) {
                        vars.add(new VarDefinition(varDefinition.getName(), varDefinition.getDisplay(), Integer.toString(getVar(varDefinition.getName())), varDefinition.getType(), varDefinition.getDisplayType(), varDefinition.getModify(), varDefinition.getOrder()));
                    } else if (varDefinition.getType() == VarType.TIMER) {
                        vars.add(new VarDefinition(varDefinition.getName(), varDefinition.getDisplay(), Integer.toString(getVar(varDefinition.getName())), varDefinition.getType(), varDefinition.getDisplayType(), varDefinition.getModify(), varDefinition.getOrder()));
                    }
                }
                break;
            }
        }
        return vars;
    }

    private VarDefinition getVarDefinition(String name) {
        for (VarDefinition varDefinition : scriptDefinition.getVars()) {
            if (varDefinition.getName().equals(name)) {
                return varDefinition;
            }
        }
        return null;
    }

    public void updateVariable(String key, String value) {
        VarDefinition definition = getVarDefinition(key);

        if (definition != null) {
            int v;
            switch (definition.getDisplayType()) {
                case BOOLEAN:
                case STANDARD: {
                    v = Integer.parseInt(value);
                }
                break;
                case TENTH: {

                }
                return;
                case SECONDS: {
                    String[] parts = value.split(":");
                    int multiply = 1;
                    v = 0;
                    for (int i = parts.length - 1; i >= 0; i--) {
                        int part = Integer.parseInt(parts[i]) * multiply;
                        v += part;
                        multiply *= 60;
                    }
                }
                break;
                default:
                    return;
            }


            vars.put(key, v);
        }
    }

    public void pressComponent(String componentId, ActionType actionType) {
        switch (actionType) {
            case SWIPE_UP:
            case SLOW_UP:
            case SWIPE_DOWN:
            case SLOW_DOWN:
            case SLOW_LEFT:
            case SLOW_RIGHT:
            case SWIPE_LEFT:
            case SWIPE_RIGHT:
            case TAP: {
                ComponentDefinition componentDefinition = components.get(componentId);
                if (componentDefinition == null) {
                    throw new RuntimeException("Cannot find component with id: " + componentId);
                }
                AdbUtils.component(deviceDefinition, componentDefinition, actionType, shell, false);
            }
            break;
        }

    }
}
