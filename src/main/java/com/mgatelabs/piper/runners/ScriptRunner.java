package com.mgatelabs.piper.runners;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mgatelabs.piper.shared.details.ActionDefinition;
import com.mgatelabs.piper.shared.details.ActionType;
import com.mgatelabs.piper.shared.details.ComponentDefinition;
import com.mgatelabs.piper.shared.details.ConditionDefinition;
import com.mgatelabs.piper.shared.details.ConnectionDefinition;
import com.mgatelabs.piper.shared.details.DeviceDefinition;
import com.mgatelabs.piper.shared.details.ScreenDefinition;
import com.mgatelabs.piper.shared.details.ScriptEnvironment;
import com.mgatelabs.piper.shared.details.StateCallType;
import com.mgatelabs.piper.shared.details.StateDefinition;
import com.mgatelabs.piper.shared.details.StateResult;
import com.mgatelabs.piper.shared.details.StatementDefinition;
import com.mgatelabs.piper.shared.details.VarDefinition;
import com.mgatelabs.piper.shared.details.VarDisplay;
import com.mgatelabs.piper.shared.details.VarModify;
import com.mgatelabs.piper.shared.details.VarTierDefinition;
import com.mgatelabs.piper.shared.details.ViewDefinition;
import com.mgatelabs.piper.shared.helper.DeviceHelper;
import com.mgatelabs.piper.shared.helper.InfoTransfer;
import com.mgatelabs.piper.shared.helper.MapTransfer;
import com.mgatelabs.piper.shared.helper.PointTransfer;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.image.RawImageWrapper;
import com.mgatelabs.piper.shared.image.SamplePoint;
import com.mgatelabs.piper.shared.image.Sampler;
import com.mgatelabs.piper.shared.image.StateTransfer;
import com.mgatelabs.piper.shared.util.AdbShell;
import com.mgatelabs.piper.shared.util.AdbUtils;
import com.mgatelabs.piper.shared.util.IntVar;
import com.mgatelabs.piper.shared.util.Loggers;
import com.mgatelabs.piper.shared.util.Mather;
import com.mgatelabs.piper.shared.util.StringVar;
import com.mgatelabs.piper.shared.util.Var;
import com.mgatelabs.piper.shared.util.VarInstance;
import com.mgatelabs.piper.shared.util.VarManager;
import com.mgatelabs.piper.shared.util.VarTimer;
import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017 for Phone-Piper
 */
public class ScriptRunner {

    public enum Status {
        INIT,
        READY,
        RUNNING,
        PAUSED,
        STOPPED
    }

    private final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final Pattern SINGLE_VARIABLE = Pattern.compile("^\\$\\{[a-zA-Z0-9_-]+\\}$");

    private static final NumberFormat THREE_DECIMAL = new DecimalFormat("#.###");

    private ConnectionDefinition connectionDefinition;
    private ScriptEnvironment scriptEnvironment;
    private DeviceDefinition deviceDefinition;
    private ViewDefinition viewDefinition;

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

    public ScriptRunner(ConnectionDefinition connectionDefinition, DeviceHelper deviceHelper, ScriptEnvironment scriptEnvironment, DeviceDefinition deviceDefinition, ViewDefinition viewDefinition) {
        this.scriptEnvironment = scriptEnvironment;
        this.deviceDefinition = deviceDefinition;
        this.connectionDefinition = connectionDefinition;
        this.viewDefinition = viewDefinition;
        vars = new VarManager(logger);
        timers = Maps.newHashMap();
        stack = new Stack<>();

        shell = new AdbShell(deviceDefinition);
        shell.attachhandler(Loggers.webLogger);
        shell.attachhandler(Loggers.fileLogger);

        logger.removeHandler(Loggers.webLogger);
        logger.addHandler(Loggers.webLogger);

        logger.removeHandler(Loggers.fileLogger);
        logger.addHandler(Loggers.fileLogger);

        Level min = Loggers.webLogger.getLevel().intValue() < Loggers.fileLogger.getLevel().intValue() ? Loggers.webLogger.getLevel() : Loggers.fileLogger.getLevel();

        logger.setLevel(min);
        shell.setLevel(min);

        logger.finer("Extracting Variables");

        List<VarDefinition> varDefinitions = Lists.newArrayList(scriptEnvironment.getVarDefinitions());
        vars.global(varDefinitions);

        for (VarDefinition varDefinition : varDefinitions) {
            if (varDefinition.getDisplayType() == VarDisplay.SECONDS && varDefinition.getModify() != VarModify.EDITABLE) {
                timers.put(varDefinition.getName(), new VarTimer(false));
            }
        }

        varDefinitions.sort(new Comparator<VarDefinition>() {
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

        logger.finer("Generating State Info");

        transferStateMap = Maps.newHashMap();
        transferStateMap.putAll(generateStateInfo());

        transferMap = new MapTransfer();
        //ComponentDefinition miniMapArea = components.get("dungeon-mini_map-area");
        //ComponentDefinition miniMapAreaCenter = components.get("dungeon-mini_map-center");
        //transferMap.setup(deviceDefinition.getWidth(), 12, 4, miniMapArea.getW(), miniMapArea.getH(), miniMapArea.getX(), miniMapArea.getY(), miniMapAreaCenter.getW(), miniMapAreaCenter.getH());

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

    private Var getVar(String name) {
        return vars.get(name);
    }

    private void putVar(String name, Var data) {
        vars.update(name, data);
        if (timers.containsKey(name) && data.toInt() == 0) {
            timers.get(name).reset();
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

        for (Map.Entry<String, StateDefinition> stateEntry : scriptEnvironment.getFilteredStates().entrySet()) {
            StateTransfer stateTransfer = new StateTransfer();
            stateTransfer.setStateId(stateEntry.getKey());
            List<String> determinedScreenIds = Lists.newArrayList();

            List<String> unfilteredScreenIds = stateEntry.getValue().determineScreenIds(ImmutableSet.of(), scriptEnvironment.getStateDefinitions());
            Set<String> tempScreenIds = Sets.newHashSet();

            for (String tempScreenId : unfilteredScreenIds) {
                if (tempScreenId.contains("$")) {
                    String regex = replaceTokensForRegex(tempScreenId);
                    Pattern p = Pattern.compile(regex);
                    for (ScreenDefinition screenDefinition : viewDefinition.getScreens()) {
                        if (p.matcher(screenDefinition.getScreenId()).matches()) {
                            tempScreenIds.add(screenDefinition.getScreenId());
                        }
                    }
                } else {
                    tempScreenIds.add(tempScreenId);
                }
            }

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

            StateDefinition stateDefinition = scriptEnvironment.getStateDefinitions().get(stateName);

            if (stateDefinition == null) {
                logger.log(Level.SEVERE, "Cannot find state with id: " + stateName);
                setStatus(Status.INIT);
                throw new RuntimeException("Cannot find state with id: " + stateName);
            } else {
                logger.log(Level.FINE, "Found initial state with id: " + stateName);
            }

            // Always reset the loop counter
            putVar(VAR_LOOPS, IntVar.ZERO);

            vars.state(stateDefinition, Maps.newHashMap(), logger);

            while (isRunning()) {

                for (VarDefinition varDefinition : getRawEditVariables()) {
                    if (varDefinition.getDisplayType() == VarDisplay.SECONDS && varDefinition.getModify() != VarModify.EDITABLE) {
                        final VarTimer timer = timers.get(varDefinition.getName());
                        timer.forward();
                        vars.update(varDefinition.getName(), new IntVar((int) TimeUnit.NANOSECONDS.toSeconds(timer.getElapsed())));
                    }
                }

                if (!shell.isReady()) {
                    logger.log(Level.WARNING, "Bad Shell: Will try to reconnect...");
                    if (connectionDefinition.isWifi()) {
                        waitFor(1000);
                        AdbShell.connect(deviceHelper.getIpAddress(), connectionDefinition.getAdbPort());
                    }
                    waitFor(1000);
                    restartShell();
                    waitFor(1000);
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

                    logger.fine("Helper Image Persisted in " + THREE_DECIMAL.format(lastImageDuration) + "s");

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
                        logger.fine("USB Image Persisted in " + THREE_DECIMAL.format(lastImageDuration) + "s");
                    }
                }

                boolean keepRunning = true;

                while (keepRunning && isRunning()) {
                    keepRunning = false;

                    final StateResult result = getStateResult(stateDefinition, imageWrapper, 0);

                    switch (result.getType()) {
                        case STOP: {
                            this.status = Status.STOPPED;
                            return;
                        }
                        case MOVE: {
                            stateDefinition = scriptEnvironment.getStateDefinitions().get(result.getValue());
                            if (stateDefinition == null) {
                                logger.log(Level.SEVERE, "Cannot find state with id: " + result.getValue());
                                throw new RuntimeException("Cannot find state with id: " + result.getValue());
                            }
                            // If this state had arguments, set them up now before altering the state
                            final Map<String, String> stateArguments = Maps.newHashMap();
                            for (Map.Entry<String, String> entry : result.getActionDefinition().getArguments().entrySet()) {
                                stateArguments.put(entry.getKey(), replaceTokens(entry.getValue()));
                            }
                            logger.fine("Running State: " + stateDefinition.getName());
                            vars.state(stateDefinition, stateArguments, logger);
                            currentStateId = stateDefinition.getId();
                            keepRunning = false;
                        }
                        break;
                        case REPEAT: {
                            keepRunning = false;
                        }
                        break;
                    }
                }

                if (connectionDefinition.getThrottle() > 0)
                    waitFor(connectionDefinition.getThrottle());
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

            long startTime = System.nanoTime();
            validScreenIds = deviceHelper.check(stateDefinition.getId());
            long endTime = System.nanoTime();

            long dif = endTime - startTime;

            lastImageDate = new Date();
            lastImageDuration = ((float) dif / 1000000000.0f);

            logger.fine("Screen State checked in " + THREE_DECIMAL.format(lastImageDuration) + "s");

            if (logger.getLevel() == Level.FINEST) {
                logger.log(Level.FINEST, "Valid Screens: " + Joiner.on(",").join(validScreenIds));
            }
        }

        return state(stateDefinition, imageWrapper, startingAction, StateCallType.STATE, ImmutableMap.of(), false);
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
        if (value == null) return new StringVar("");
        if (SINGLE_VARIABLE.matcher(value).matches()) {
            // This is a single variable lookup, extract the name and look it up
            return getVar(value.substring(2, value.length() - 1));
        } else if (value.startsWith("$") && value.indexOf("{") == -1) {
            // Old Style Value's
            return getVar(value.substring(1));
        }
        // Replace any token with a value and continue
        return new StringVar(replaceTokens(value));
    }

    private boolean stillRunning() {
        return status == Status.RUNNING;
    }

    private StateResult state(final StateDefinition stateDefinition, final ImageWrapper imageWrapper, int startingAction, StateCallType callType, Map<String, String> arguments, boolean inBatch) {
        if (callType == StateCallType.CALL) {
            logger.fine("Calling State: " + stateDefinition.getName());
            vars.push(stateDefinition, arguments);
        } else if (callType == StateCallType.CONDITION) {
            logger.fine("Condition State: " + stateDefinition.getName());
            vars.push(stateDefinition, arguments);
        }

        boolean batchCmds = false;
        StateResult priorResult;
        StateResult stateResult = null;
        for (StatementDefinition statementDefinition : stateDefinition.getStatements()) {
            boolean checkStatus = check(statementDefinition.getCondition(), imageWrapper);
            if (logger.isLoggable(Level.FINEST) && statementDefinition.getCondition() != null) {
                logger.finest("CHECK: " + ConditionDefinition.getConditionString(statementDefinition.getCondition()) + " - " + checkStatus);
            }
            if (checkStatus) {
                for (ActionDefinition actionDefinition : statementDefinition.getActions()) {
                    final int actionIndex = statementDefinition.getActions().indexOf(actionDefinition);
                    if (actionIndex < startingAction)
                        continue;

                    // Skip actions not allowed for the current state mode
                    if (callType == StateCallType.CALL && !actionDefinition.getType().isAllowedForCall()) {
                        continue;
                    } else if (callType == StateCallType.STATE && !actionDefinition.getType().isAllowedForState()) {
                        continue;
                    } else if (callType == StateCallType.CONDITION && !actionDefinition.getType().isAllowedForCondition()) {
                        continue;
                    }

                    // Actions can have conditions
                    if (actionDefinition.getCondition() != null && !check(actionDefinition.getCondition(), imageWrapper)) {
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.finest("Action Skip: " + ConditionDefinition.getConditionString(actionDefinition.getCondition()));
                        }
                        continue;
                    } else if (actionDefinition.getCondition() != null) {
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.finest("Action Run: " + ConditionDefinition.getConditionString(actionDefinition.getCondition()));
                        }
                    }

                    priorResult = stateResult;
                    stateResult = new StateResult(actionDefinition.getType(), actionDefinition, priorResult, actionIndex, stateDefinition);

                    final int loopIndex;
                    if (!StringUtils.isEmpty(actionDefinition.getCount())) {
                        Var v = valueHandler(actionDefinition.getCount());
                        loopIndex = v.toInt();
                    } else {
                        loopIndex = 1;
                    }

                    for (int looper = 0; looper < loopIndex; looper++) {
                        if (!stillRunning()) {
                            return new StateResult(ActionType.STOP, actionDefinition, priorResult, actionIndex, stateDefinition);
                        }

                        switch (actionDefinition.getType()) {
                            case INFO: {
                                String msg = replaceTokens(actionDefinition.getValue());
                                logger.info("MSG: " + msg);
                            }
                            break;
                            case FINER: {
                                String msg = replaceTokens(actionDefinition.getValue());
                                logger.finer("MSG: " + msg);
                            }
                            break;
                            case FINEST: {
                                String msg = replaceTokens(actionDefinition.getValue());
                                logger.finest("MSG: " + msg);
                            }
                            break;
                            case BATCH: {
                                if (inBatch) {
                                    logger.log(Level.FINEST, "Skipping batch request, already in batch");
                                } else if ("START".equalsIgnoreCase(actionDefinition.getValue())) {
                                    batchCmds = true;
                                } else if (batchCmds) {
                                    batchCmds = false;
                                    shell.exec();
                                }
                            }
                            break;
                            case COMPONENT: {
                                ComponentDefinition componentDefinition = components.get(replaceTokens(actionDefinition.getValue()));
                                if (componentDefinition == null) {
                                    logger.log(Level.SEVERE, "Cannot find component with id: " + actionDefinition.getValue());
                                    throw new RuntimeException("Cannot find component with id: " + actionDefinition.getValue());
                                }
                                if (actionDefinition.getArguments().containsKey("w")) {
                                    putVar(actionDefinition.getArguments().get("w"), new IntVar(componentDefinition.getW()));
                                }
                                if (actionDefinition.getArguments().containsKey("h")) {
                                    putVar(actionDefinition.getArguments().get("h"), new IntVar(componentDefinition.getH()));
                                }
                                if (actionDefinition.getArguments().containsKey("x")) {
                                    putVar(actionDefinition.getArguments().get("x"), new IntVar(componentDefinition.getX()));
                                }
                                if (actionDefinition.getArguments().containsKey("y")) {
                                    putVar(actionDefinition.getArguments().get("y"), new IntVar(componentDefinition.getY()));
                                }
                            }
                            break;
                            case PIXEL: {
                                Var x = null, y = null;
                                if (actionDefinition.getArguments().containsKey("x")) {
                                    x = valueHandler(actionDefinition.getArguments().get("x"));
                                }
                                if (actionDefinition.getArguments().containsKey("y")) {
                                    y = valueHandler(actionDefinition.getArguments().get("y"));
                                }
                                if (x == null || y == null) {
                                    logger.log(Level.SEVERE, "Cannot execute pixel request, x & y arguments are required");
                                    throw new RuntimeException("Cannot execute pixel request, x & y arguments are required");
                                }
                                final Sampler sample = new Sampler();
                                if (deviceHelper != null) {
                                    int[] pixels = deviceHelper.pixel(RawImageWrapper.getOffsetFor(deviceDefinition.getWidth(), 12, x.toInt(), y.toInt(), RawImageWrapper.ImageFormats.RGBA));
                                    if (pixels != null) {
                                        sample.setR(pixels[0]);
                                        sample.setG(pixels[1]);
                                        sample.setB(pixels[2]);
                                    }
                                } else {
                                    imageWrapper.getPixel(x.toInt(), y.toInt(), sample);
                                }
                                if (actionDefinition.getArguments().containsKey("r")) {
                                    putVar(actionDefinition.getArguments().get("r"), new IntVar(sample.getR()));
                                }
                                if (actionDefinition.getArguments().containsKey("g")) {
                                    putVar(actionDefinition.getArguments().get("g"), new IntVar(sample.getG()));
                                }
                                if (actionDefinition.getArguments().containsKey("b")) {
                                    putVar(actionDefinition.getArguments().get("b"), new IntVar(sample.getB()));
                                }
                            }
                            break;
                            case SET: {
                                String varName = actionDefinition.getVar();
                                Var value = valueHandler(actionDefinition.getValue());
                                putVar(varName, value);
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
                                putVar(varName, orig.add(value));
                            }
                            break;
                            case MATH: {
                                String varName = actionDefinition.getVar();
                                VarInstance varInstance = vars.getVarInstance(varName);
                                Var expr = valueHandler(actionDefinition.getValue());
                                String expression = expr.toString();
                                Var result = Mather.evaluate(expression, varInstance.getType());
                                if (logger.isLoggable(Level.FINEST)) {
                                    logger.finest("MATH: " + expression + " = " + result.toString());
                                }
                                putVar(varName, result);
                            }
                            break;
                            case RANDOM: {
                                final String varName = actionDefinition.getVar();
                                Var min = IntVar.ZERO;
                                Var max = IntVar.THOUSAND;
                                if (actionDefinition.getArguments().containsKey("min")) {
                                    min = new StringVar(replaceTokens(actionDefinition.getArguments().get("min")));
                                }
                                if (actionDefinition.getArguments().containsKey("max")) {
                                    max = new StringVar(replaceTokens(actionDefinition.getArguments().get("max")));
                                }
                                if (min.greater(max) || min.equals(max)) {
                                    logger.log(Level.SEVERE, "Random needs a min and max that are different and min must be less than max");
                                    throw new RuntimeException("Random needs a min and max that are different and min must be less than max");
                                }
                                int bound = max.toInt() - min.toInt();
                                int newValue = SECURE_RANDOM.nextInt(bound);
                                if (logger.isLoggable(Level.FINEST)) {
                                    logger.finest("RANDOM(" + min.toString() + ", " + max.toString() + ") = " + (min.toInt() + newValue));
                                }
                                putVar(varName, new IntVar(min.toInt() + newValue));
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
                                if (!AdbUtils.event(actionDefinition.getValue(), false, shell, batchCmds || inBatch)) {
                                    logger.log(Level.SEVERE, "Unknown event id: " + actionDefinition.getValue());
                                    throw new RuntimeException("Unknown event id: " + actionDefinition.getValue());
                                }
                            }
                            break;
                            case INPUT: {
                                if (!AdbUtils.event(valueHandler(actionDefinition.getValue()).toString(), true, shell, batchCmds || inBatch)) {
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
                                final StateDefinition callDefinition = scriptEnvironment.getStateDefinitions().get(callName);
                                final Map<String, String> callArguments = Maps.newHashMap();
                                for (Map.Entry<String, String> entry : actionDefinition.getArguments().entrySet()) {
                                    callArguments.put(entry.getKey(), replaceTokens(entry.getValue()));
                                }
                                final StateResult callResult = state(callDefinition, imageWrapper, 0, StateCallType.CALL, callArguments, batchCmds);
                                vars.pop();
                                if (callResult.getResult() != null && !StringUtils.isEmpty(actionDefinition.getVar())) {
                                    putVar(actionDefinition.getVar(), callResult.getResult());
                                }
                            }
                            break;
                            case RETURN: {
                                if (!StringUtils.isEmpty(actionDefinition.getValue())) {
                                    stateResult.setResult(valueHandler(actionDefinition.getValue()));
                                }
                            } // Let it fall through
                            case MOVE:
                            case REPEAT:
                            case STOP:
                                return stateResult;
                        }
                    }
                }
            }
        }
        if (callType != StateCallType.STATE) {
            return StateResult.RETURN;
        }
        return StateResult.REPEAT;
    }

    private String replaceTokens(String text) {
        if (text != null) {
            int startIndex;
            while ((startIndex = text.indexOf("${")) >= 0) {
                int endIndex = text.indexOf('}', startIndex);
                if (endIndex > startIndex + 2) {
                    String varName = text.substring(startIndex + 2, endIndex).trim();
                    if (varName.length() > 0) {
                        if (vars.getVarInstance(varName) != null) {
                            text = text.substring(0, startIndex) + vars.get(varName) + text.substring(endIndex + 1);
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

    private String replaceTokensForRegex(String text) {
        if (text != null) {
            int startIndex;
            while ((startIndex = text.indexOf("${")) >= 0) {
                int endIndex = text.indexOf('}', startIndex);
                if (endIndex > startIndex + 2) {
                    text = text.substring(0, startIndex) + "[a-zA-Z0-9_-]+" + text.substring(endIndex + 1);
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
                Var value = valueHandler(conditionDefinition.getValue());
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
            case CALL: {
                final String callName = conditionDefinition.getValue();
                if (!callName.startsWith("@")) {
                    throw new RuntimeException("All condition calls must start with a @: " + conditionDefinition.getValue());
                }
                final StateDefinition callDefinition = scriptEnvironment.getStateDefinitions().get(callName);
                final Map<String, String> callArguments = Maps.newHashMap();
                for (Map.Entry<String, String> entry : conditionDefinition.getArguments().entrySet()) {
                    callArguments.put(entry.getKey(), replaceTokens(entry.getValue()));
                }
                final StateResult callResult = state(callDefinition, imageWrapper, 0, StateCallType.CONDITION, callArguments, false);
                vars.pop();
                if (callResult.getResult() == null) {
                    throw new RuntimeException("All condition calls must return a 0 or 1");
                }
                result = callResult.getResult().toInt() == 1;
            }
            break;
            case SCREEN: {
                Var screenValue = valueHandler(conditionDefinition.getValue());
                ScreenDefinition screenDefinition = screens.get(screenValue.toString());
                if (screenDefinition == null || !screenDefinition.isEnabled() || screenDefinition.getPoints() == null || screenDefinition.getPoints().isEmpty()) {
                    failure = true;
                } else {
                    if (deviceHelper != null) {
                        result = validScreenIds.contains(screenDefinition.getScreenId());
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
        for (VarDefinition varDefinition : scriptEnvironment.getVarDefinitions()) {
            switch (varDefinition.getModify()) {
                case HIDDEN:
                    continue;
                case VISIBLE:
                case EDITABLE:
                    Var var = getVar(varDefinition.getName());
                    if (var == null) {
                        var = IntVar.ZERO;
                    }
                    final VarDefinition copy = new VarDefinition(varDefinition);
                    copy.setValue(var.toString());
                    vars.add(copy);
                    break;
            }
        }
        return vars;
    }

    public List<VarTierDefinition> getVariableTiers() {
        List<VarTierDefinition> vars = Lists.newArrayList();
        vars.addAll(scriptEnvironment.getVarTiers());
        return vars;
    }

    public List<VarDefinition> getRawEditVariables() {
        List<VarDefinition> vars = Lists.newArrayList();
        for (VarDefinition varDefinition : scriptEnvironment.getVarDefinitions()) {
            switch (varDefinition.getModify()) {
                case HIDDEN:
                    continue;
                case VISIBLE:
                case EDITABLE: {
                    final VarDefinition copy = new VarDefinition(varDefinition);
                    copy.setValue(getVar(varDefinition.getName()).toString());
                    vars.add(copy);
                }
                break;
            }
        }
        return vars;
    }

    private VarDefinition getVarDefinition(String name) {
        for (VarDefinition varDefinition : scriptEnvironment.getVarDefinitions()) {
            if (varDefinition.getName().equals(name)) {
                return varDefinition;
            }
        }
        return null;
    }

    public void updateVariableFromUserInput(String key, String value) {
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
                    if (StringUtils.isBlank(value)) {
                        return;
                    }
                    float f = Float.parseFloat(value);
                    f *= 10;
                    v = new IntVar((int) f);
                }
                break;
                case SECONDS: {
                    String[] parts = value.split(":");
                    int multiply = 1;
                    v = IntVar.ZERO;
                    for (int i = parts.length - 1; i >= 0; i--) {
                        int part = Integer.parseInt(parts[i]) * multiply;
                        v = v.add(new IntVar(part));
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
