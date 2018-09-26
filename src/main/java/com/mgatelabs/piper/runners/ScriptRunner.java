package com.mgatelabs.piper.runners;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mgatelabs.piper.shared.details.*;
import com.mgatelabs.piper.shared.helper.DeviceHelper;
import com.mgatelabs.piper.shared.helper.InfoTransfer;
import com.mgatelabs.piper.shared.helper.MapTransfer;
import com.mgatelabs.piper.shared.helper.PointTransfer;
import com.mgatelabs.piper.shared.image.*;
import com.mgatelabs.piper.shared.util.*;
import com.mgatelabs.piper.ui.utils.WebLogHandler;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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

    private static final Pattern SINGLE_VARIABLE = Pattern.compile("^\\$\\{[a-zA-Z0-9_-]+\\}$");

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

    private Stack<StateResult> stack;

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private DeviceHelper deviceHelper;

    private Set<String> validScreenIds;

    private AdbShell shell;

    private VarManager vars;

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
        vars = new VarManager(logger);
        timers = Maps.newHashMap();
        stack = new Stack<>();
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
            min = webLogHandler.getLevel().intValue() < fileHandler.getLevel().intValue() ? webLogHandler.getLevel() : fileHandler.getLevel();
        }
        logger.setLevel(webLogHandler.getLevel());
        shell.setLevel(webLogHandler.getLevel());


        logger.finer("Extracting Variables");

        elapsedTime = 0;

        // Script Includes
        Map<String, ScriptDefinition> scriptDefinitions = Maps.newLinkedHashMap();
        scriptDefinitions.put(scriptDefinition.getScriptId(), scriptDefinition);
        scriptDefinitions.putAll(buildScriptDefinitionsFromIncludes(scriptDefinition.getIncludes()));

        if (!CollectionUtils.isEmpty(scriptDefinition.getStates())) {
            for (StateDefinition stateDefinition : scriptDefinition.getStates().values()) {
                if (!scriptDefinitions.containsKey(scriptDefinition.getScriptId())) {
                    scriptDefinitions.putAll(buildScriptDefinitionsFromIncludes(stateDefinition.getIncludes()));
                }
            }
        }

        for (ScriptDefinition otherDefinition : scriptDefinitions.values()) {
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
            if (varDefinition.getDisplayType() == VarDisplay.SECONDS) {
                timers.put(varDefinition.getName(), new VarTimer(false));
            }
        }
        vars.global(scriptDefinition.getVars());

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

    private Map<String, ScriptDefinition> buildScriptDefinitionsFromIncludes(List<String> includes) {
        Map<String, ScriptDefinition> scriptDefinitions = Maps.newLinkedHashMap();
        for (String scriptId : includes) {
            if (scriptDefinitions.containsKey(scriptId))
                continue;

            if (scriptId.trim().length() > 0) {
                ScriptDefinition scriptDef = ScriptDefinition.read(scriptId);
                if (scriptDef == null) {
                    continue;
                } else {
                    scriptDefinitions.put(scriptId, scriptDef);
                }

                Map<String, StateDefinition> otherStates = scriptDef.getStates();
                if (!CollectionUtils.isEmpty(otherStates)) {
                    for (StateDefinition otherState : otherStates.values()) {
                        scriptDefinitions.putAll(buildScriptDefinitionsFromIncludes(otherState.getIncludes()));
                    }
                }
            }
        }
        return scriptDefinitions;
    }

    private void setupStateDefinitions(Map<String, ScriptDefinition> scriptDefinitionMap) {
        for (ScriptDefinition scriptDefinition : scriptDefinitionMap.values()) {
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
        }
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

    private Var getVar(String name) {
        return vars.get(name);
    }

    private void putVar(String name, Var data) {
        vars.update(name, data);
    }

    private void setVar(String name, Var value) {
        final VarDefinition varDefinition = getVarDefinition(name);
        switch (varDefinition.getDisplayType()) {
            case SECONDS: {
                if (value.toInt() == 0) {
                    VarTimer timer = timers.get(varDefinition.getName());
                    timer.reset();
                    putVar(name, IntVar.ZERO);
                }
            }
            break;
            default:
                putVar(name, value);
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

            // Skip all include and function states
            if (stateEntry.getKey().startsWith("_") || stateEntry.getKey().startsWith("@")) continue;

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
            setVar(VAR_LOOPS, IntVar.ZERO);

            while (isRunning()) {

                for (VarDefinition varDefinition : getRawEditVariables()) {
                    if (varDefinition.getDisplayType() == VarDisplay.SECONDS) {
                        final VarTimer timer = timers.get(varDefinition.getName());
                        timer.forward();
                        vars.update(varDefinition.getName(), new IntVar((int) TimeUnit.NANOSECONDS.toSeconds(timer.getElapsed())));
                    }
                }

                if (!shell.isReady()) {
                    logger.log(Level.WARNING, "Bad Shell: Will try to reconnect...");
                    if (connectionDefinition.isWifi()) {
                        Thread.sleep(1000);
                        AdbShell.connect(deviceHelper.getIpAddress());
                    }
                    Thread.sleep(1000);
                    restartShell();
                    Thread.sleep(1000);
                }

                if (deviceHelper != null) {
                    long startTime = System.nanoTime();

                    if (!AdbUtils.persistScreen(shell)) {
                        logger.warning("Helper Image Failure");
                        waitFor(250);
                        continue;
                    }

                    long endTime = System.nanoTime();

                    long dif = endTime - startTime;

                    lastImageDate = new Date();
                    lastImageDuration = ((float) dif / 1000000000.0f);

                    logger.finest("Helper Image Persisted in " + lastImageDuration);

                    imageWrapper = null;
                } else {
                    long startTime = System.nanoTime();
                    imageWrapper = AdbUtils.getScreen();
                    long endTime = System.nanoTime();

                    long dif = endTime - startTime;

                    lastImageDate = new Date();
                    lastImageDuration = ((float) dif / 1000000000.0f);

                    if (imageWrapper == null || !imageWrapper.isReady()) {
                        logger.warning("USB Image Failure");
                        waitFor(250);
                        continue;
                    } else {
                        logger.finest("USB Image Persisted in " + lastImageDuration);
                    }
                }

                boolean keepRunning = true;

                while (keepRunning && isRunning()) {
                    keepRunning = false;

                    StateResult result = getStateResult(stateDefinition, imageWrapper, 0);
                    if (result.getType() == ActionType.POP) {
                        if (stack.size() > 0) {
                            final StateResult oldState = stack.pop();
                            stateDefinition = scriptDefinition.getStates().get(oldState.getStateDefinition().getId());
                            if (stateDefinition == null) {
                                logger.log(Level.SEVERE, "Cannot find state with id: " + oldState);
                                throw new RuntimeException("Cannot find state with id: " + oldState);
                            }
                            currentStateId = stateDefinition.getId();
                            keepRunning = false;

                            result = getStateResult(stateDefinition, imageWrapper, oldState.getActionIndex() + 1);
                        } else {
                            throw new RuntimeException("Stack is empty");
                        }
                    }

                    switch (result.getType()) {
                        case STOP: {
                            this.status = Status.STOPPED;
                            return;
                        }
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
                            stack.push(result);
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

    private StateResult getStateResult(StateDefinition stateDefinition, ImageWrapper imageWrapper, int startingAction) {
        if (deviceHelper != null) {
            logger.finer("Helper: /check/" + stateDefinition.getId());

            validScreenIds = deviceHelper.check(stateDefinition.getId());

            if (logger.getLevel() == Level.FINEST) {
                logger.log(Level.FINEST, "Valid Screens: " + Joiner.on(",").join(validScreenIds));
            }
        }

        return state(stateDefinition, imageWrapper, startingAction, false, ImmutableMap.of(), false);
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

    private Var valueHandler(String value) {
        if (SINGLE_VARIABLE.matcher(value).matches()) {
            // This is a single variable lookup, extract the name and look it up
            return getVar(value.substring(2, value.length() - 1));
        } else if (value.startsWith("$")) {
            // Old Style Value's
            return getVar(value.substring(1));
        }
        // Replace any token with a value and continue
        return new StringVar(replaceTokens(value));
    }

    private boolean stillRunning() {
        return status == Status.RUNNING;
    }

    private StateResult state(final StateDefinition stateDefinition, final ImageWrapper imageWrapper, int startingAction, boolean isCall, Map<String, String> arguments, boolean inBatch) {
        if (isCall) {
            logger.fine("Calling State: " + stateDefinition.getName());
            vars.push(stateDefinition, arguments);
        } else {
            logger.fine("Running State: " + stateDefinition.getName());
            vars.state(stateDefinition, arguments);
        }

        boolean batchCmds = false;
        StateResult priorResult = null;
        StateResult stateResult = null;
        for (StatementDefinition statementDefinition : stateDefinition.getStatements()) {
            if (check(statementDefinition.getCondition(), imageWrapper)) {
                for (ActionDefinition actionDefinition : statementDefinition.getActions()) {
                    final int actionIndex = statementDefinition.getActions().indexOf(actionDefinition);
                    if (actionIndex < startingAction)
                        continue;

                    priorResult = stateResult;
                    stateResult = new StateResult(actionDefinition.getType(), actionDefinition, priorResult, actionIndex, stateDefinition);
                    final int loopIndex = actionDefinition.getCount() <= 0 ? 1 : actionDefinition.getCount();

                    for (int looper = 0; looper < loopIndex; looper++) {
                        if (!stillRunning()) {
                            return new StateResult(ActionType.STOP, actionDefinition, priorResult, actionIndex, stateDefinition);
                        }

                        // Skip actions not allowed for the current state mode
                        if (isCall && !actionDefinition.getType().isAllowedForCall()) {
                            continue;
                        } else if (!isCall && !actionDefinition.getType().isAllowedForState()) {
                            continue;
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
                                            if (vars.getVarInstance(varName) != null) {
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
                                if (inBatch) {
                                    logger.log(Level.FINEST, "Skipping batch request, already in batch");
                                }
                                else if ("START".equalsIgnoreCase(actionDefinition.getValue())) {
                                    batchCmds = true;
                                } else if (batchCmds) {
                                    batchCmds = false;
                                    shell.exec();
                                }
                            }
                            break;
                            case SET: {
                                String varName = actionDefinition.getVar();
                                Var value = valueHandler(actionDefinition.getValue());
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
                                Var value = valueHandler(actionDefinition.getValue());
                                Var orig = vars.get(varName);
                                setVar(varName, orig.add(value));
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
                                ComponentDefinition componentDefinition = components.get(replaceTokens(actionDefinition.getValue()));
                                if (componentDefinition == null) {
                                    logger.log(Level.SEVERE, "Cannot find component with id: " + actionDefinition.getValue());
                                    throw new RuntimeException("Cannot find component with id: " + actionDefinition.getValue());
                                }
                                logger.finest("Performing Action " + actionDefinition.getType() + " For Component: " + componentDefinition.getComponentId());
                                AdbUtils.component(deviceDefinition, componentDefinition, actionDefinition.getType(), shell, batchCmds || inBatch);
                            }
                            break;

                            case EVENT: {
                                if (!AdbUtils.event(actionDefinition.getValue(), shell, batchCmds || inBatch)) {
                                    logger.log(Level.SEVERE, "Unknown event id: " + actionDefinition.getValue());
                                    throw new RuntimeException("Unknown event id: " + actionDefinition.getValue());
                                }
                            }
                            break;
                            case INPUT: {
                                if (!AdbUtils.event(valueHandler(actionDefinition.getValue()).toString(), shell, batchCmds || inBatch)) {
                                    logger.log(Level.SEVERE, "Unknown event id: " + actionDefinition.getValue());
                                    throw new RuntimeException("Unknown event id: " + actionDefinition.getValue());
                                }
                            }
                            break;

                            case WAIT: {
                                int time = valueHandler(actionDefinition.getValue()).toInt();
                                if (time > 0) {
                                    waitFor(time);
                                } else {
                                    logger.log(Level.SEVERE, "Invalid wait time: " + actionDefinition.getValue());
                                    throw new RuntimeException("Invalid wait time: " + actionDefinition.getValue());
                                }
                            }
                            break;
                            case CALL: {
                                // Call Names are not dynamic
                                final String callName = actionDefinition.getValue();
                                if (!callName.startsWith("@")) {
                                    throw new RuntimeException("All calls must start with a @: " + actionDefinition.getValue());
                                }
                                final StateDefinition callDefinition = scriptDefinition.getStates().get(callName);
                                final Map<String, String> callArguments = Maps.newHashMap();
                                for (Map.Entry<String, String> entry : actionDefinition.getArguments().entrySet()) {
                                    callArguments.put(entry.getKey(), replaceTokens(entry.getValue()));
                                }
                                final StateResult callResult = state(callDefinition, imageWrapper, 0, true, callArguments, batchCmds);
                                vars.pop();
                                if (callResult.getResult() != null && !StringUtils.isEmpty(actionDefinition.getVar())) {
                                    setVar(actionDefinition.getVar(), callResult.getResult());
                                }
                            }
                            break;
                            case RETURN: {
                                if (!StringUtils.isEmpty(actionDefinition.getValue())) {
                                    stateResult.setResult(valueHandler(actionDefinition.getValue()));
                                }
                            } // Let it fall through
                            case POP:
                            case PUSH:
                            case SWAP:
                            case MOVE:
                            case REPEAT:
                            case STOP:
                                return stateResult;
                        }
                    }
                }
            }
        }
        return StateResult.REPEAT;
    }

    private String replaceTokens(String text) {
        if (text != null) {
            int startindex;
            while ((startindex = text.indexOf("${")) >= 0) {
                int endIndex = text.indexOf('}', startindex);
                if (endIndex > startindex + 2) {
                    String varName = text.substring(startindex += 2, endIndex).trim();
                    if (varName.length() > 0) {
                        if (vars.getVarInstance(varName) != null) {
                            text = text.substring(0, startindex - 2) + vars.get(varName) + text.substring(endIndex + 1);
                        } else {
                            break;
                        }
                    }
                } else {
                    break;
                }
            }
        }
        return text;
    }

    private void waitFor(long milli) {
        try {
            Thread.sleep(milli);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean check(final ConditionDefinition conditionDefinition, ImageWrapper imageWrapper) {
        if (conditionDefinition == null) return true;
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
                Var value;
                if (conditionDefinition.getValue().startsWith("$")) {
                    value = getVar(conditionDefinition.getValue().substring(1));
                } else {
                    value = new StringVar(conditionDefinition.getValue());
                }
                String varName = conditionDefinition.getVar();
                Var currentValue = getVar(varName);
                switch (conditionDefinition.getUsedCondition()) {
                    case GREATER: {
                        result = currentValue.greater(value);
                    }
                    break;
                    case LESS: {
                        result = currentValue.lesser(value);
                    }
                    break;
                    case EQUAL: {
                        result = currentValue.equals(value);
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
                Var energy;
                if (conditionDefinition.getVar() != null && conditionDefinition.getVar().trim().length() > 0) {
                    energy = getVar(conditionDefinition.getVar());
                } else {
                    energy = new StringVar(conditionDefinition.getValue());
                }
                energy = energy.asInt();
                if (energy.toInt() >= PlayerDefinition.MIN_ENERGY && energy.toInt() <= PlayerDefinition.MAX_ENERGY) {
                    float requiredPercent = (energy.toFloat() / (float) playerDefinition.getTotalEnergy());
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
                case EDITABLE:
                    Var var = getVar(varDefinition.getName());
                    if (var == null) {
                        var = IntVar.ZERO;
                    }
                    vars.add(new VarDefinition(varDefinition.getName(), varDefinition.getDisplay(), var.toString(), varDefinition.getType(), varDefinition.getDisplayType(), varDefinition.getModify(), varDefinition.getOrder()));
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
                    vars.add(new VarDefinition(varDefinition.getName(), varDefinition.getDisplay(), getVar(varDefinition.getName()).toString(), varDefinition.getType(), varDefinition.getDisplayType(), varDefinition.getModify(), varDefinition.getOrder()));
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
            Var v;
            switch (definition.getDisplayType()) {
                case BOOLEAN:
                case STANDARD: {
                    v = new StringVar(value);
                }
                break;
                case TENTH: {

                }
                return;
                case SECONDS: {
                    String[] parts = value.split(":");
                    int multiply = 1;
                    v = IntVar.ZERO;
                    for (int i = parts.length - 1; i >= 0; i--) {
                        int part = Integer.parseInt(parts[i]) * multiply;
                        v.add(new IntVar(part));
                        multiply *= 60;
                    }
                }
                break;
                default:
                    return;
            }
            vars.update(key, v);
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
