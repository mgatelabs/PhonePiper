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
import com.mgatelabs.piper.shared.details.ExecutableLink;
import com.mgatelabs.piper.shared.details.ProcessingStateInfo;
import com.mgatelabs.piper.shared.details.ScreenDefinition;
import com.mgatelabs.piper.shared.details.ScriptEnvironment;
import com.mgatelabs.piper.shared.details.StateCallType;
import com.mgatelabs.piper.shared.details.StateLink;
import com.mgatelabs.piper.shared.details.StateResult;
import com.mgatelabs.piper.shared.details.StateType;
import com.mgatelabs.piper.shared.details.StatementDefinition;
import com.mgatelabs.piper.shared.details.VarDefinition;
import com.mgatelabs.piper.shared.details.VarDisplay;
import com.mgatelabs.piper.shared.details.VarModify;
import com.mgatelabs.piper.shared.details.VarTabDefinition;
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
import com.mgatelabs.piper.shared.util.AdbWrapper;
import com.mgatelabs.piper.shared.util.IntVar;
import com.mgatelabs.piper.shared.util.Mather;
import com.mgatelabs.piper.shared.util.StringVar;
import com.mgatelabs.piper.shared.util.Var;
import com.mgatelabs.piper.shared.util.VarInstance;
import com.mgatelabs.piper.shared.util.VarManager;
import com.mgatelabs.piper.shared.util.VarTimer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Used to run an ScriptEnvironment program
 * <p>
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017 for Phone-Piper
 */
public class ScriptRunner {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public enum Status {
        INIT,
        READY,
        RUNNING,
        PAUSED,
        STOPPED
    }

    private final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final Pattern SINGLE_VARIABLE = Pattern.compile("^\\$\\{[a-zA-Z0-9_-]+\\}$");

    public static final NumberFormat THREE_DECIMAL = new DecimalFormat("#.###");

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

    private AdbWrapper shell;

    private VarManager vars;

    private volatile Status status;

    private Date lastImageDate;
    private float lastImageDuration;

    private Map<String, VarTimer> timers;

    //private static final String VAR_LOOPS = "_loops";

    public ScriptRunner(ConnectionDefinition connectionDefinition, DeviceHelper deviceHelper, ScriptEnvironment scriptEnvironment, DeviceDefinition deviceDefinition, ViewDefinition viewDefinition, AdbWrapper adbWrapper) {
        this.scriptEnvironment = scriptEnvironment;
        this.deviceDefinition = deviceDefinition;
        this.connectionDefinition = connectionDefinition;
        this.viewDefinition = viewDefinition;
        vars = new VarManager();
        timers = Maps.newHashMap();
        stack = new Stack<>();

        shell = adbWrapper;

        logger.debug("Extracting Variables");

        List<VarDefinition> varDefinitions = Lists.newArrayList(scriptEnvironment.getVarDefinitions().values());
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

        logger.debug("Extracting Screens");

        screens = Maps.newHashMap();
        for (ScreenDefinition screenDefinition : viewDefinition.getScreens()) {
            screens.put(screenDefinition.getScreenId(), screenDefinition);
        }

        logger.debug("Extracting Components");

        components = Maps.newHashMap();
        for (ComponentDefinition componentDefinition : viewDefinition.getComponents()) {
            components.put(componentDefinition.getComponentId(), componentDefinition);
        }

        logger.debug("Generating State Info");

        transferStateMap = Maps.newHashMap();
        transferStateMap.putAll(generateStateInfo());

        transferMap = new MapTransfer();

        validScreenIds = null;

        this.deviceHelper = deviceHelper;

        status = Status.INIT;
    }

    public void stopShell() {
        /*
        if (shell != null) {
            try {
                shell.shutdown();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            shell = null;
        }
        */
    }

    public void restartShell() {
        // Kill the Server
        AdbShell.killServer();
        // Bring it back up
        AdbShell.devices();
        // Bring the shell back up
        shell.connect();
    }

    public Date getLastImageDate() {
        return lastImageDate;
    }

    public float getLastImageDuration() {
        return lastImageDuration;
    }

    public boolean initHelper() {
        if (deviceHelper == null) {
            logger.error("Phone Helper connection is down, please restart app");
            return false;
        }
        if (deviceHelper.ready()) {
            logger.info("Phone Helper is ready @ " + deviceHelper.getIpAddress());
            InfoTransfer infoTransfer = new InfoTransfer();
            infoTransfer.setStates(transferStateMap);
            infoTransfer.setMap(transferMap);
            if (deviceHelper.setup(infoTransfer)) {
                logger.info("Phone Helper is configured");
                return true;
            } else {
                logger.info("Phone Helper is not configured");
            }
        } else {
            logger.error("Phone Helper is not configured");
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

        final ImmutableMap<String, ExecutableLink> executables = scriptEnvironment.getExecutableStates(ImmutableSet.of(StateType.STATE, StateType.FUNCTION));

        for (Map.Entry<String, ExecutableLink> executionEntry : scriptEnvironment.getExecutableStates(ImmutableSet.of(StateType.STATE)).entrySet()) {
            StateTransfer stateTransfer = new StateTransfer();
            stateTransfer.setStateId(executionEntry.getKey());
            List<String> determinedScreenIds = Lists.newArrayList();

            Set<String> unfilteredScreenIds = executionEntry.getValue().getLink().determineStateIds(ImmutableSet.of(), executables);
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
                    logger.error("Unknown Screen Id: " + screeId);
                    continue;
                } else if (screenDefinition.getPoints() == null) {
                    System.out.println("Bad Screen Id: " + screeId);
                    logger.error("Bad Screen Id: " + screeId);
                    continue;
                }
                if (!screenDefinition.isEnabled() || screenDefinition.getPoints() == null || (screenDefinition.getPoints() != null && screenDefinition.getPoints().isEmpty())) {
                    logger.debug("Disabled Screen: " + screenDefinition.getScreenId() + " for state: " + executionEntry.getValue().getId());
                    continue;
                }
                boolean valid = true;
                for (SamplePoint point : screenDefinition.getPoints()) {
                    if (point.getX() > deviceDefinition.getViewWidth() || point.getY() >= deviceDefinition.getViewHeight()) {
                        valid = false;
                        logger.debug("Invalid Screen Point for Screen: " + screenDefinition.getScreenId());
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
                    points.add(new PointTransfer(RawImageWrapper.getOffsetFor(deviceDefinition.getViewWidth(), 12, point.getX(), point.getY(), RawImageWrapper.ImageFormats.RGBA), (byte) i, (byte) point.getR(), (byte) point.getG(), (byte) point.getB()));
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

            logger.debug("Init Helper");

            initHelper();

            ImageWrapper imageWrapper;

            ExecutableLink currentExecutionLink = scriptEnvironment.getExecutableState(stateName);

            if (currentExecutionLink == null) {
                logger.error("Cannot find executable with id: " + stateName);
                setStatus(Status.INIT);
                throw new RuntimeException("Cannot find executable state with id: " + stateName);
            } else {
                logger.debug("Found initial executable with id: " + stateName);
            }

            // Always reset the loop counter
            //putVar(VAR_LOOPS, IntVar.ZERO);

            vars.state(currentExecutionLink, Maps.newHashMap());

            while (isRunning()) {

                for (VarDefinition varDefinition : getRawEditVariables()) {
                    if (varDefinition.getDisplayType() == VarDisplay.SECONDS && varDefinition.getModify() != VarModify.EDITABLE) {
                        final VarTimer timer = timers.get(varDefinition.getName());
                        timer.forward();
                        vars.update(varDefinition.getName(), new IntVar((int) TimeUnit.NANOSECONDS.toSeconds(timer.getElapsed())));
                    }
                }

                deviceHelper.refresh(shell);
                imageWrapper = null;

                boolean keepRunning = true;

                while (keepRunning && isRunning()) {
                    keepRunning = false;

                    final StateResult result = getStateResult(currentExecutionLink, imageWrapper);

                    switch (result.getType()) {
                        case STOP: {
                            this.status = Status.STOPPED;
                            return;
                        }
                        case MOVE: {
                            currentExecutionLink = scriptEnvironment.getExecutableState(result.getValue());
                            if (currentExecutionLink == null) {
                                logger.error("Cannot find state with id: " + result.getValue());
                                throw new RuntimeException("Cannot find state with id: " + result.getValue());
                            }
                            // If this state had arguments, set them up now before altering the state
                            final Map<String, String> stateArguments = Maps.newHashMap();
                            for (Map.Entry<String, String> entry : result.getActionDefinition().getArguments().entrySet()) {
                                stateArguments.put(entry.getKey(), replaceTokens(entry.getValue()));
                            }
                            logger.debug("Running State: " + currentExecutionLink.getName());
                            vars.state(currentExecutionLink, stateArguments);
                            currentStateId = currentExecutionLink.getId();
                            keepRunning = false;
                        }
                        break;
                        case SOFT_REPEAT:
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
            logger.error(ex.getMessage());
            ex.printStackTrace();
        } finally {
            setStatus(Status.STOPPED);
            logger.info("Script Stopped");
        }
    }

    private StateResult getStateResult(ExecutableLink executableState, ImageWrapper imageWrapper) {
        refreshViews(false);
        Stack<ProcessingStateInfo> stateStack = new Stack<>();
        stateStack.push(new ProcessingStateInfo(executableState.getLink()));
        StateResult result;
        try {
            result = executeState(stateStack, executableState, imageWrapper, StateCallType.STATE, ImmutableMap.of(), false);
        } catch (Exception ex) {
            logger.error("Exception: {} {}", ex.getMessage(), logStackTraceInfo(stateStack));
            // Auto STOP
            return StateResult.STOP(stateStack);
        }
        stateStack.pop();
        return result;
    }

    private synchronized void refreshViews(boolean captureAgain) {
        if (deviceHelper != null) {

            if (captureAgain) {
                if (!deviceHelper.refresh(shell)) {
                    return;
                }
            }

            long startTime = System.nanoTime();
            validScreenIds = deviceHelper.check(vars.getCurrentSceneId());
            long endTime = System.nanoTime();

            long dif = endTime - startTime;

            lastImageDate = new Date();
            lastImageDuration = ((float) dif / 1000000000.0f);

            logger.debug("Screen State checked in " + THREE_DECIMAL.format(lastImageDuration) + "s");
            logger.trace("Valid Screens: " + Joiner.on(",").join(validScreenIds));
        } else {
            logger.error("Unable to REFRESH screen");
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

    /**
     * This get a executable state ready to run
     */
    private StateResult executeState(Stack<ProcessingStateInfo> stateStack, final ExecutableLink executableState, final ImageWrapper imageWrapper, StateCallType callType, Map<String, String> arguments, boolean inBatch) {
        if (callType == StateCallType.CALL) {
            vars.push(executableState, arguments);
        } else if (callType == StateCallType.CONDITION) {
            vars.push(executableState, arguments);
        }

        StateResult stateResult = executableStateProcessor(stateStack, executableState.getLink(), imageWrapper, callType, inBatch);

        return stateResult;
    }

    /**
     * Generate a basic trace
     */
    private String logStackTraceInfo(final Stack<ProcessingStateInfo> stack, String additionalMessages) {
        final StringBuilder sb = new StringBuilder();
        if (logger.isTraceEnabled()) {
            logger.trace("{}", new Object() {
                @Override
                public String toString() {
                    String string = logStackTraceInfo(stack);
                    sb.append(string);
                    sb.append(' ').append(additionalMessages).toString();
                    return sb.toString();
                }
            });
        }
        return sb.append(' ').append(additionalMessages).toString();
    }

    /**
     * Generate a basic trace
     */
    private String logStackTraceInfo(Stack<ProcessingStateInfo> stack) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stack.size(); i++) {
            ProcessingStateInfo state = stack.get(i);
            if (i > 0) {
                sb.append(" - ");
            }
            sb.append(state.getLink().getScriptId()).append(".").append(state.getLink().getState().getId()).append("[").append(state.getStateIndex());
            if (state.getActionIndex() >= 0)
                sb.append(",").append(state.getActionIndex());
            else if (state.getActionIndex() == -1)
                sb.append(", condition");
            else if (state.getActionIndex() == -2)
                sb.append(", includes");
            sb.append("] ");
        }
        return sb.toString();
    }

    private StateResult executableStateProcessor(Stack<ProcessingStateInfo> stateStack, StateLink executableState, final ImageWrapper imageWrapper, StateCallType callType, boolean inBatch) {
        boolean batchCmds = false;
        StateResult priorResult;
        StateResult stateResult = null;
        final ProcessingStateInfo stateTracker = stateStack.peek();
        int statementIndex = 0;
        for (StatementDefinition statementDefinition : executableState.getState().getStatements()) {
            stateTracker.setStateIndex(statementIndex++);
            stateTracker.setActionIndex(-1);
            boolean checkStatus = check(stateStack, statementDefinition.getCondition(), imageWrapper);
            logStackTraceInfo(stateStack, "CHECK: " + ConditionDefinition.getConditionString(statementDefinition.getCondition()) + " - " + checkStatus);

            if (checkStatus) {
                int actionIndex = 0;
                int programIndex = 0;
                while (programIndex < statementDefinition.getActions().size()) {
                    ActionDefinition actionDefinition = statementDefinition.getActions().get(programIndex++);
                    stateTracker.setActionIndex(actionIndex++);
                    // Skip actions not allowed for the current state mode
                    if (callType == StateCallType.CALL && !actionDefinition.getType().isAllowedForCall()) {
                        logger.trace("Action " + actionDefinition.getType() + " has been skipped");
                        continue;
                    } else if (callType == StateCallType.STATE && !actionDefinition.getType().isAllowedForState()) {
                        logger.trace("Action " + actionDefinition.getType() + " has been skipped");
                        continue;
                    } else if (callType == StateCallType.CONDITION && !actionDefinition.getType().isAllowedForCondition()) {
                        logger.trace("Action " + actionDefinition.getType() + " has been skipped");
                        continue;
                    }

                    // Actions can have conditions
                    if (actionDefinition.getCondition() != null && !check(stateStack, actionDefinition.getCondition(), imageWrapper)) {
                        logStackTraceInfo(stateStack, "Action Skipped: " + ConditionDefinition.getConditionString(actionDefinition.getCondition()));
                        continue;
                    } else if (actionDefinition.getCondition() != null) {
                        logStackTraceInfo(stateStack, "Action Allowed: " + ConditionDefinition.getConditionString(actionDefinition.getCondition()));
                    }

                    if (actionDefinition.getType() == ActionType.CONTINUE) {
                        // Get out of the current statement block
                        break;
                    }

                    priorResult = stateResult;
                    stateResult = new StateResult(actionDefinition.getType(), actionDefinition, priorResult, stateStack);

                    final int loopMax;
                    if (!StringUtils.isEmpty(actionDefinition.getCount())) {
                        Var v = valueHandler(actionDefinition.getCount());
                        loopMax = v.toInt();
                    } else {
                        loopMax = 1;
                    }

                    for (int loopIndex = 0; loopIndex < loopMax; loopIndex++) {
                        if (!stillRunning()) {
                            return new StateResult(ActionType.STOP, actionDefinition, priorResult, stateStack);
                        }

                        switch (actionDefinition.getType()) {
                            case INFO: {
                                String msg = replaceTokens(actionDefinition.getValue());
                                logger.info(logStackTraceInfo(stateStack, "MSG: " + msg));
                            }
                            break;
                            case FINER: {
                                String msg = replaceTokens(actionDefinition.getValue());
                                logger.debug(logStackTraceInfo(stateStack, "FINER: " + msg));
                            }
                            break;
                            case FINEST: {
                                String msg = replaceTokens(actionDefinition.getValue());
                                logStackTraceInfo(stateStack, "FINEST: " + msg);
                            }
                            break;
                            case BATCH: {
                                if (inBatch) {
                                    logStackTraceInfo(stateStack, "Skipping batch request, already in batch");
                                } else if ("START".equalsIgnoreCase(actionDefinition.getValue())) {
                                    batchCmds = true;
                                } else if (batchCmds) {
                                    batchCmds = false;
                                    shell.exec();
                                }
                            }
                            break;
                            case DATE: {
                                Calendar c;
                                if (actionDefinition.getArguments().containsKey("tz")) {
                                    c = Calendar.getInstance(TimeZone.getTimeZone(actionDefinition.getArguments().get("tz")));
                                } else {
                                    c = Calendar.getInstance();
                                }
                                if (actionDefinition.getArguments().containsKey("y")) {
                                    putVar(actionDefinition.getArguments().get("y"), new IntVar(c.get(Calendar.YEAR)));
                                }
                                if (actionDefinition.getArguments().containsKey("m")) {
                                    putVar(actionDefinition.getArguments().get("m"), new IntVar(c.get(Calendar.MONTH) + 1));
                                }
                                if (actionDefinition.getArguments().containsKey("d")) {
                                    putVar(actionDefinition.getArguments().get("d"), new IntVar(c.get(Calendar.DATE)));
                                }
                            }
                            break;
                            case TIME: {
                                Calendar c;
                                if (actionDefinition.getArguments().containsKey("tz")) {
                                    c = Calendar.getInstance(TimeZone.getTimeZone(actionDefinition.getArguments().get("tz")));
                                } else {
                                    c = Calendar.getInstance();
                                }
                                if (actionDefinition.getArguments().containsKey("h")) {
                                    putVar(actionDefinition.getArguments().get("h"), new IntVar(c.get(Calendar.HOUR_OF_DAY)));
                                }
                                if (actionDefinition.getArguments().containsKey("m")) {
                                    putVar(actionDefinition.getArguments().get("m"), new IntVar(c.get(Calendar.MINUTE) + 1));
                                }
                                if (actionDefinition.getArguments().containsKey("s")) {
                                    putVar(actionDefinition.getArguments().get("s"), new IntVar(c.get(Calendar.SECOND)));
                                }
                            }
                            break;
                            case COMPONENT: {
                                ComponentDefinition componentDefinition = components.get(replaceTokens(actionDefinition.getValue()));
                                if (componentDefinition == null) {
                                    logger.error("Cannot find component with id: " + actionDefinition.getValue());
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
                                    logger.error("Cannot execute pixel request, x & y arguments are required");
                                    throw new RuntimeException("Cannot execute pixel request, x & y arguments are required");
                                }
                                final Sampler sample = new Sampler();
                                if (deviceHelper != null) {
                                    int[] pixels = deviceHelper.pixel(RawImageWrapper.getOffsetFor(deviceDefinition.getViewWidth(), 12, x.toInt(), y.toInt(), RawImageWrapper.ImageFormats.RGBA));
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
                                logStackTraceInfo(stateStack, " MATH: " + expression + " = " + result.toString());
                                putVar(varName, result);
                            }
                            break;
                            case LABEL: {
                                // NO OP
                                logger.trace("Found Label: " + actionDefinition.getValue());
                            }
                            break;
                            case GOTO: {
                                logger.trace("Going to Label: " + actionDefinition.getValue());
                                for (int i = 0; i < statementDefinition.getActions().size(); i++) {
                                    ActionDefinition inspectAction = statementDefinition.getActions().get(i);
                                    if (inspectAction.getType() == ActionType.LABEL && inspectAction.getValue().equals(actionDefinition.getValue())) {
                                        // Set the program counter to this label's index, so it will be proceed next
                                        programIndex = i;
                                        break;
                                    }
                                }
                            }
                            break;
                            case REFRESH: {
                                // Simply tell the system to refresh the view, this may take a second
                                refreshViews(true);
                            }
                            break;
                            case RANDOM: {
                                final String varName = actionDefinition.getVar();
                                Var min = IntVar.ZERO;
                                Var max = IntVar.THOUSAND;
                                if (actionDefinition.getArguments().containsKey("min")) {
                                    min = new StringVar(replaceTokens(actionDefinition.getArguments().get("min"))).asInt();
                                }
                                if (actionDefinition.getArguments().containsKey("max")) {
                                    max = new StringVar(replaceTokens(actionDefinition.getArguments().get("max"))).asInt();
                                }
                                if (min.greater(max) || min.equals(max)) {
                                    logger.error("Random needs a min and max that are different and min must be less than max");
                                    throw new RuntimeException("Random needs a min and max that are different and min must be less than max");
                                }
                                int bound = max.toInt() - min.toInt();
                                int newValue = SECURE_RANDOM.nextInt(bound);
                                logStackTraceInfo(stateStack, "RANDOM(" + min.toString() + ", " + max.toString() + ") = " + (min.toInt() + newValue));
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
                                    logger.error("Cannot find component with id: " + actionDefinition.getValue());
                                    throw new RuntimeException("Cannot find component with id: " + actionDefinition.getValue());
                                }
                                logger.trace("Performing Action " + actionDefinition.getType() + " For Component: " + componentDefinition.getComponentId());
                                AdbUtils.component(deviceDefinition, componentDefinition, actionDefinition.getType(), shell, batchCmds || inBatch);
                            }
                            break;

                            case EVENT: {
                                if (!AdbUtils.event(actionDefinition.getValue(), false, shell, batchCmds || inBatch)) {
                                    logger.error("Unknown event id: " + actionDefinition.getValue());
                                    throw new RuntimeException("Unknown event id: " + actionDefinition.getValue());
                                }
                            }
                            break;
                            case INPUT: {
                                if (!AdbUtils.event(valueHandler(actionDefinition.getValue()).toString(), true, shell, batchCmds || inBatch)) {
                                    logger.error("Unknown event id: " + actionDefinition.getValue());
                                    throw new RuntimeException("Unknown event id: " + actionDefinition.getValue());
                                }
                            }
                            break;

                            case WAIT: {
                                int time = valueHandler(actionDefinition.getValue()).toInt();
                                if (time > 0) {
                                    waitFor(time);
                                } else if (time < 0) {
                                    logger.error("Invalid wait time: " + actionDefinition.getValue() + " = " + time);
                                    throw new RuntimeException("Invalid wait time: " + actionDefinition.getValue() + " = " + time);
                                }
                            }
                            break;
                            case CALL: {
                                // Call Names are not dynamic
                                final String callName = actionDefinition.getValue();
                                if (!callName.startsWith("@")) {
                                    throw new RuntimeException("All calls must start with a @: " + actionDefinition.getValue());
                                }
                                final ExecutableLink callDefinition = scriptEnvironment.getExecutableState(callName);
                                final Map<String, String> callArguments = Maps.newHashMap();
                                for (Map.Entry<String, String> entry : actionDefinition.getArguments().entrySet()) {
                                    callArguments.put(entry.getKey(), replaceTokens(entry.getValue()));
                                }
                                stateStack.push(new ProcessingStateInfo(callDefinition.getLink()));
                                final StateResult callResult = executeState(stateStack, callDefinition, imageWrapper, StateCallType.CALL, callArguments, batchCmds);

                                logStackTraceInfo(callResult.getStack(), " " + callResult.toString());

                                stateStack.pop();
                                vars.pop();
                                if (callResult.getResult() != null && !StringUtils.isEmpty(actionDefinition.getVar())) {
                                    putVar(actionDefinition.getVar(), callResult.getResult());
                                }
                            }
                            break;
                            case LINK: {
                                // Pretend we're doing imports
                                for (StateLink children : actionDefinition.getLinks()) {
                                    stateStack.push(new ProcessingStateInfo(children));
                                    StateResult childResult = executableStateProcessor(stateStack, children, imageWrapper, callType, inBatch);
                                    stateStack.pop();
                                    if (childResult.getType() != ActionType.SOFT_REPEAT) {
                                        return childResult;
                                    }
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
            return StateResult.RETURN(stateStack);
        }

        for (StateLink children : executableState.getIncludes()) {
            stateTracker.setActionIndex(-2);
            stateStack.push(new ProcessingStateInfo(children));
            StateResult childResult = executableStateProcessor(stateStack, children, imageWrapper, callType, inBatch);
            stateStack.pop();
            if (childResult.getType() != ActionType.SOFT_REPEAT) {
                return childResult;
            }
        }

        return StateResult.SOFT_REPEAT(stateStack);
    }

    /**
     * Look at the given text and replace any token with variables
     */
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

    /**
     * Turn tokens into REGEX for searching purposes
     */
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
            final long startTime = System.nanoTime();
            final long endTime = startTime + TimeUnit.MILLISECONDS.toNanos(milli);

            while (true) {
                if (System.nanoTime() >= endTime) return;
                if (!isRunning()) {
                    return;
                }
                Thread.sleep(25);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Perform a condition check
     */
    private boolean check(Stack<ProcessingStateInfo> stateStack, final ConditionDefinition conditionDefinition, ImageWrapper imageWrapper) {
        if (conditionDefinition == null) return true;
        boolean result = false;
        boolean checkAnd = true;
        boolean failure = false;

        try {
            MDC.put("conditionDefinition.getVar()", conditionDefinition.getVar());
            MDC.put("conditionDefinition.getValue()", conditionDefinition.getValue());
            MDC.put("valueHandler(conditionDefinition.getValue())", valueHandler(conditionDefinition.getValue()).toString());
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
                    final ExecutableLink callDefinition = scriptEnvironment.getExecutableState(callName);
                    final Map<String, String> callArguments = Maps.newHashMap();
                    for (Map.Entry<String, String> entry : conditionDefinition.getArguments().entrySet()) {
                        callArguments.put(entry.getKey(), replaceTokens(entry.getValue()));
                    }
                    stateStack.push(new ProcessingStateInfo(callDefinition.getLink()));
                    final StateResult callResult = executeState(stateStack, callDefinition, imageWrapper, StateCallType.CONDITION, callArguments, false);
                    stateStack.pop();

                    logStackTraceInfo(callResult.getStack(), " " + callResult.toString());

                    vars.pop();
                    if (callResult.getResult() == null) {
                        throw new RuntimeException("All condition calls must return a 0 or 1");
                    }
                    result = callResult.getResult().toInt() == 1;
                }
                break;
                case SCREEN: {
                    for (String value : conditionDefinition.getValues()) {
                        Var screenValue = valueHandler(value);
                        ScreenDefinition screenDefinition = screens.get(screenValue.toString());
                        if (screenDefinition == null || !screenDefinition.isEnabled() || screenDefinition.getPoints() == null || screenDefinition.getPoints().isEmpty()) {
                            failure = true;
                            break;
                        } else {
                            if (deviceHelper != null) {
                                result = validScreenIds.contains(screenDefinition.getScreenId());
                            } else {
                                if (screenDefinition == null) {
                                    logger.error("Cannot find screen with id: " + conditionDefinition.getValue());
                                    throw new RuntimeException("Cannot find screen with id: " + conditionDefinition.getValue());
                                }
                                result = SamplePoint.validate(screenDefinition.getPoints(), imageWrapper, false);
                            }
                        }
                        // On the first success, break out of the loop
                        if (result) break;
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
                    if (!check(stateStack, sub, imageWrapper)) {
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
                    if (check(stateStack, sub, imageWrapper)) {
                        result = true;
                        break;
                    }
                }
            }

            // If we failed, but have a OR, check the OR
            if (!result && !conditionDefinition.getOr().isEmpty()) {
                for (ConditionDefinition sub : conditionDefinition.getOr()) {
                    if (check(stateStack, sub, imageWrapper)) {
                        result = true;
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw t;
        } finally {
            MDC.clear();
        }
        return result;
    }

    public List<VarDefinition> getVariables() {
        List<VarDefinition> vars = Lists.newArrayList();
        for (VarDefinition varDefinition : scriptEnvironment.getVarDefinitions().values()) {
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
                    if (varDefinition.getVarValueId() != null && varDefinition.getVarValueId().length() > 0) {
                        copy.setValues(scriptEnvironment.getVarValues(varDefinition.getVarValueId()));
                    }
                    vars.add(copy);
                    break;
            }
        }
        return vars;
    }

    public Map<String, String> getStateVariables() {
        final Map<String, String> result = Maps.newHashMap();
        final Map<String, VarInstance> stateVars = vars.GetStateVariables();
        for (Map.Entry<String, VarInstance> entry : stateVars.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getVar().toString());
        }
        return result;
    }

    public List<VarTierDefinition> getVariableTiers() {
        List<VarTierDefinition> vars = Lists.newArrayList();
        vars.addAll(scriptEnvironment.getVarTiers().values());
        vars.sort(new Comparator<VarTierDefinition>() {
            @Override
            public int compare(VarTierDefinition o1, VarTierDefinition o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        return vars;
    }

    public List<VarTabDefinition> getVariableTabs() {
        List<VarTabDefinition> vars = Lists.newArrayList();
        vars.addAll(scriptEnvironment.getVarTabs().values());
        vars.sort(new Comparator<VarTabDefinition>() {
            @Override
            public int compare(VarTabDefinition o1, VarTabDefinition o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        return vars;
    }

    public List<VarDefinition> getRawEditVariables() {
        List<VarDefinition> vars = Lists.newArrayList();
        for (VarDefinition varDefinition : scriptEnvironment.getVarDefinitions().values()) {
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
        return scriptEnvironment.getVarDefinitions().get(name);
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

    public String getDefaultVariableValue(String key) {
        VarDefinition definition = getVarDefinition(key);
        if (definition != null) {
            return definition.getValue();
        }
        return "";
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
