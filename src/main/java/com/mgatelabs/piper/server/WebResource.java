package com.mgatelabs.piper.server;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.mgatelabs.piper.Runner;
import com.mgatelabs.piper.runners.ScriptRunner;
import com.mgatelabs.piper.server.actions.CacheScreenAction;
import com.mgatelabs.piper.server.actions.EditActionInterface;
import com.mgatelabs.piper.server.actions.EditComponentAction;
import com.mgatelabs.piper.server.actions.EditScreenAction;
import com.mgatelabs.piper.server.actions.FixScreenAction;
import com.mgatelabs.piper.server.actions.LiveVerifyScreenAction;
import com.mgatelabs.piper.server.actions.RepairScreenAction;
import com.mgatelabs.piper.server.actions.StubComponentAction;
import com.mgatelabs.piper.server.actions.StubScreenAction;
import com.mgatelabs.piper.server.actions.UpdateComponentImageAction;
import com.mgatelabs.piper.server.actions.UpdateScreenAction;
import com.mgatelabs.piper.server.actions.VerifyScreenAction;
import com.mgatelabs.piper.server.entities.ConfigListResponse;
import com.mgatelabs.piper.server.entities.FileListResult;
import com.mgatelabs.piper.server.entities.LoadRequest;
import com.mgatelabs.piper.server.entities.NamedValueDescriptionItem;
import com.mgatelabs.piper.server.entities.NamedValueItem;
import com.mgatelabs.piper.server.entities.PrepResult;
import com.mgatelabs.piper.server.entities.StatusLog;
import com.mgatelabs.piper.server.entities.StatusResult;
import com.mgatelabs.piper.server.entities.ValueResult;
import com.mgatelabs.piper.shared.ScriptThread;
import com.mgatelabs.piper.shared.details.ActionType;
import com.mgatelabs.piper.shared.details.ComponentDefinition;
import com.mgatelabs.piper.shared.details.ConnectionDefinition;
import com.mgatelabs.piper.shared.details.ExecutableLink;
import com.mgatelabs.piper.shared.details.ScreenDefinition;
import com.mgatelabs.piper.shared.details.StateDefinition;
import com.mgatelabs.piper.shared.details.StateType;
import com.mgatelabs.piper.shared.details.VarDefinition;
import com.mgatelabs.piper.shared.details.VarModify;
import com.mgatelabs.piper.shared.details.VarStateDefinition;
import com.mgatelabs.piper.shared.helper.Closer;
import com.mgatelabs.piper.shared.helper.DeviceHelper;
import com.mgatelabs.piper.shared.helper.LocalDeviceHelper;
import com.mgatelabs.piper.shared.helper.NoOpDeviceHelper;
import com.mgatelabs.piper.shared.helper.RemoteDeviceHelper;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.image.Sampler;
import com.mgatelabs.piper.shared.util.AdbShell;
import com.mgatelabs.piper.shared.util.AdbUtils;
import com.mgatelabs.piper.shared.util.AdbWrapper;
import com.mgatelabs.piper.shared.util.JsonTool;
import com.mgatelabs.piper.shared.util.Loggers;
import com.mgatelabs.piper.ui.FrameChoices;
import com.mgatelabs.piper.ui.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.imageio.ImageIO;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by @mgatelabs (Michael Fuller) on 2/13/2018.
 */
@Path("/piper")
public class WebResource {

    // This can change
    private static ScriptRunner runner;
    private static ScriptThread thread;

    private static EditHolder editHolder;

    // These don't change
    private static ConnectionDefinition connectionDefinition;
    private static FrameChoices frameChoices;
    private static DeviceHelper deviceHelper;
    private static AdbWrapper adbWrapper;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private synchronized boolean checkInitialState() {
        if (connectionDefinition == null) {
            Loggers.init();
            connectionDefinition = new ConnectionDefinition();
            deviceHelper = new RemoteDeviceHelper(connectionDefinition);
            return true;
        }
        return false;
    }

    @GET
    @Path("/")
    @Produces("text/html")
    public String control() {
        checkInitialState();

        URL url = Resources.getResource("web/pages/index.html");

        try {
            return Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    @POST
    @Path("/variable")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized void setVariable(@FormParam("key") String key, @FormParam("value") String value) {
        checkInitialState();
        if (runner != null) {
            runner.updateVariableFromUserInput(key, value);

            VarStateDefinition varStateDefinition = new VarStateDefinition();
            for (VarDefinition varDefinition : runner.getVariables()) {
                if (varDefinition.getModify() == VarModify.EDITABLE && !varDefinition.isSkipSave()) {
                    varStateDefinition.addItem(varDefinition);
                }
            }
            if (varStateDefinition.getItems().size() > 0) {
                varStateDefinition.save(frameChoices.getStateNameOrDefault());
            }
        }
    }

    @POST
    @Path("/variables")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized void setVariables(@FormParam("content") String contentStr) {
        try {
            final ObjectMapper objectMapper = JsonTool.getInstance();
            TypeReference<HashMap<String, String>> typeRef
                    = new TypeReference<HashMap<String, String>>() {
            };
            Map<String, String> content = objectMapper.readValue(contentStr, typeRef);
            for (Map.Entry<String, String> entry : content.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                setVariable(key, value);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @POST
    @Path("/reset")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized void resetVariables(@FormParam("items") String itemStr) {
        if (runner != null) {
            try {
                final ObjectMapper objectMapper = JsonTool.getInstance();
                TypeReference<ArrayList<String>> typeRef
                        = new TypeReference<ArrayList<String>>() {
                };
                List<String> items = objectMapper.readValue(itemStr, typeRef);
                for (String key : items) {
                    setVariable(key, runner.getDefaultVariableValue(key));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @POST
    @Path("/button")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ValueResult buttonPress(@FormParam("componentId") String componentId, @FormParam("buttonId") String buttonId) {
        checkInitialState();
        final ValueResult valueResult = new ValueResult();
        try {
            runner.pressComponent(componentId, ActionType.valueOf(buttonId));
            valueResult.setStatus("ok");
        } catch (Exception ex) {
            valueResult.setStatus("error");
        }
        return valueResult;
    }

    @POST
    @Path("/dump/state")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized Map<String, String> dumpState() {
        if (runner != null) {
            return runner.getStateVariables();
        }
        return ImmutableMap.of();
    }

    @POST
    @Path("/adb/usb")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ValueResult adbUseUSB() {
        checkInitialState();
        final ValueResult valueResult = new ValueResult();
        try {
            adbWrapper.restart();
            String s = AdbShell.enableUsb();
            valueResult.setValue(s);
            valueResult.setStatus("ok");
        } catch (Exception ex) {
            valueResult.setStatus("error");
        }
        return valueResult;
    }

    @POST
    @Path("/adb/restart")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ValueResult adbRestart() {
        checkInitialState();
        final ValueResult valueResult = new ValueResult();
        valueResult.setValue(adbWrapper.restart());
        valueResult.setStatus("ok");
        return valueResult;
    }

    @POST
    @Path("/adb/kill")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ValueResult adbKill() {
        checkInitialState();
        final ValueResult valueResult = new ValueResult();
        if (adbWrapper != null)
            valueResult.setValue(adbWrapper.shutdown(ConnectionDefinition.AdbType.FULL));
        valueResult.setStatus("ok");
        return valueResult;
    }

    @POST
    @Path("/adb/status")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ValueResult adbStatus() {
        checkInitialState();
        final ValueResult valueResult = new ValueResult();

        if (adbWrapper != null) {
            if (adbWrapper.connect()) {
                valueResult.setValue("Connected");
                valueResult.setStatus("ok");
                return valueResult;
            }
            valueResult.setValue("Null Device");
            valueResult.setStatus("error");
        } else {
            valueResult.setValue("no adb wrapper");
        }
        return valueResult;
    }

    @POST
    @Path("/adb/appclose")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ValueResult adbAppClose() {
        checkInitialState();
        final ValueResult valueResult = new ValueResult();
        if (adbWrapper != null) {
            if (adbWrapper.connect()) {
                if (StringUtils.isNotBlank(connectionDefinition.getApp())) {
                    valueResult.setValue(adbWrapper.execWithOutput("am force-stop " + connectionDefinition.getApp()));
                    valueResult.setStatus("ok");
                    return valueResult;
                }
            }
            valueResult.setValue("No Connection");
            valueResult.setStatus("error");
        } else {
            valueResult.setValue("no adb wrapper");
        }
        return valueResult;
    }

    @POST
    @Path("/adb/appcheck")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ValueResult adbAppCheck() {
        checkInitialState();
        final ValueResult valueResult = new ValueResult();
        if (adbWrapper != null) {
            if (adbWrapper.connect()) {

                if (StringUtils.isNotBlank(connectionDefinition.getApp())) {
                    final String results = adbWrapper.execWithOutput("ps");
                    if (StringUtils.isBlank(results)) {
                        valueResult.setValue(results);
                        valueResult.setStatus("error");
                        return valueResult;
                    } else {
                        if (!results.contains(connectionDefinition.getApp())) {
                            logger.error("App: " + connectionDefinition.getApp() + " is not running, will restart");
                            valueResult.setValue(adbWrapper.execWithOutput("monkey --pct-syskeys 0 -p " + connectionDefinition.getApp() + " 1"));
                            valueResult.setStatus("ok");
                            return valueResult;
                        }
                    }
                } else {
                    valueResult.setValue("App package missing");
                    valueResult.setStatus("error");
                }

                return valueResult;
            }
            valueResult.setValue("No Connection");
            valueResult.setStatus("error");
        } else {
            valueResult.setValue("no adb wrapper");
        }
        return valueResult;
    }

    @POST
    @Path("/adb/remote")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ValueResult adbUseRemote() {
        checkInitialState();
        final ValueResult valueResult = new ValueResult();
        try {
            adbWrapper.restart();
            String s = AdbShell.enableRemote();
            valueResult.setValue(s);
            valueResult.setStatus("ok");
        } catch (Exception ex) {
            valueResult.setStatus("error");
            valueResult.setValue(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        return valueResult;
    }

    /*
    @POST
    @Path("/adb/connect")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ValueResult adbConnectRemote() {
        checkInitialState();
        final ValueResult valueResult = new ValueResult();
        try {


            String s = AdbShell.connect(deviceHelper.getIpAddress(), connectionDefinition.getAdbPort());

            valueResult.setValue(s);
            valueResult.setStatus("ok");
        } catch (Exception ex) {
            valueResult.setStatus("error");
            valueResult.setValue(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        return valueResult;
    }
    */

    @POST
    @Path("/adb/devices")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ValueResult adbDevices() {
        checkInitialState();
        final ValueResult valueResult = new ValueResult();
        try {
            String s = AdbShell.devices();
            valueResult.setValue(s);
            valueResult.setStatus("ok");
        } catch (Exception ex) {
            valueResult.setStatus("error");
            valueResult.setValue(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        return valueResult;
    }

    @POST
    @Path("/adb/reboot")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ValueResult adbReboot() {
        checkInitialState();
        final ValueResult valueResult = new ValueResult();
        try {
            String s = AdbShell.reboot();
            valueResult.setValue(s);
            valueResult.setStatus("ok");
        } catch (Exception ex) {
            valueResult.setStatus("error");
            valueResult.setValue(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        return valueResult;
    }

    @GET
    @Path("/status")
    @Produces("application/json")
    public StatusResult status() {
        StatusResult result = new StatusResult();

        if (checkInitialState()) {
            result.setStatus(StatusResult.Status.INIT);
        } else {
            if (editHolder != null) {
                result.setStatus(StatusResult.Status.EDIT_VIEW);
            } else if (runner == null) {
                result.setStatus(StatusResult.Status.INIT);
            } else {
                result.setState(runner.getCurrentStateId());
                if (thread == null) {
                    result.setStatus(StatusResult.Status.READY);
                } else {
                    if (runner.isRunning()) {
                        result.setStatus(StatusResult.Status.RUNNING);
                    } else if (runner.isStopped()) {
                        result.setStatus(StatusResult.Status.STOPPED);
                        thread = null;
                    } else {
                        result.setStatus(StatusResult.Status.STOPPING);
                    }
                }
            }
        }

        if (runner != null) {
            result.getVariables().addAll(runner.getVariables());
        }

        final ImmutableList<ILoggingEvent> records = Loggers.webHandler.getEvents();

        for (ILoggingEvent record : records) {
            StackTraceElement callerData = record.getCallerData()[0];
            String sourceName;
            if (callerData.getClassName() != null && callerData.getClassName().lastIndexOf('.') > 0) {
                sourceName = callerData.getClassName().substring(callerData.getClassName().lastIndexOf('.') + 1) + "." + callerData.getMethodName() + "(" + callerData.getLineNumber() + ")";
            } else if (record.getLoggerName() != null) {
                sourceName = record.getLoggerName();
            } else {
                sourceName = "Unknown";
            }

            result.getLogs().add(
                    new StatusLog(
                            sourceName,
                            Constants.sdf.format(new Date(record.getTimeStamp())),
                            record.getLevel().toString(),
                            record.getMessage()
                    )
            );
        }

        return result;
    }

    @POST
    @Path("/process/playPause/{stateId}")
    @Produces("application/json")
    public Map<String, String> prepProcess(@PathParam("stateId") String stateId) {

        checkInitialState();

        Map<String, String> result = Maps.newHashMap();

        if (runner != null) {
            if (thread == null) {
                thread = new ScriptThread(runner, stateId);
                thread.start();
            } else {
                runner.setStatus(ScriptRunner.Status.PAUSED);
            }
            result.put("status", "ok");
        } else {
            result.put("status", "error");
        }

        return result;
    }

    @POST
    @Path("/process/intent/pause")
    @Produces("application/json")
    public Map<String, String> intendToPause() {

        checkInitialState();

        Map<String, String> result = Maps.newHashMap();

        if (runner != null) {
            runner.setIntent(ScriptRunner.Intent.PAUSE);
            result.put("status", "ok");
        } else {
            result.put("status", "error");
        }

        return result;
    }

    @POST
    @Path("/process/prep")
    @Consumes("application/json")
    @Produces("application/json")
    public PrepResult prepProcess(@RequestBody LoadRequest request) {
        checkInitialState();

        handleConnection(request);

        if (connectionDefinition.getHelperType() == ConnectionDefinition.HelperType.REMOTE) {
            if (!(deviceHelper instanceof RemoteDeviceHelper)) {
                deviceHelper = new RemoteDeviceHelper(connectionDefinition);
            }
        } else if (connectionDefinition.getHelperType() == ConnectionDefinition.HelperType.NOOP) {
            if (!(deviceHelper instanceof NoOpDeviceHelper)) {
                deviceHelper = new NoOpDeviceHelper();
            }
        } else {
            if (!(deviceHelper instanceof LocalDeviceHelper)) {
                deviceHelper = new LocalDeviceHelper(connectionDefinition);
            }
            ((LocalDeviceHelper) deviceHelper).setUsePng(connectionDefinition.getHelperType() == ConnectionDefinition.HelperType.LOCAL_PNG);
        }

        if (adbWrapper != null) {
            adbWrapper.shutdown(connectionDefinition.getAdbLevel());
            adbWrapper = null;
        }

        thread = null;
        List<String> views = Lists.newArrayList();
        views.addAll(request.getViews());

        List<String> scripts = Lists.newArrayList();
        scripts.addAll(request.getScripts());

        frameChoices = new FrameChoices(Constants.ACTION_RUN, Constants.MODE_SCRIPT, request.getStateName(), "", request.getDevice(), views, scripts);

        if (frameChoices.isValid()) {
            final PrepResult result = new PrepResult(StatusEnum.ok);

            editHolder = null;

            setupAdbWrapper(connectionDefinition);

            result.setViewWidth(frameChoices.getDeviceDefinition().getViewWidth());
            result.setViewHeight(frameChoices.getDeviceDefinition().getViewHeight());
            result.setControlWidth(frameChoices.getDeviceDefinition().getWidth());
            result.setControlHeight(frameChoices.getDeviceDefinition().getHeight());

            runner = new ScriptRunner(connectionDefinition, deviceHelper, frameChoices.getScriptEnvironment(), frameChoices.getDeviceDefinition(), frameChoices.getViewDefinition(), adbWrapper);

            if (VarStateDefinition.exists(frameChoices.getStateNameOrDefault())) {
                VarStateDefinition varStateDefinition = VarStateDefinition.read(frameChoices.getStateNameOrDefault());
                for (VarDefinition varDefinition : varStateDefinition.getItems()) {
                    runner.updateVariableFromUserInput(varDefinition.getName(), varDefinition.getValue());
                }
            }

            return result;

        } else {
            return new PrepResult(StatusEnum.error);
        }
    }

    private void handleConnection(LoadRequest request) {
        ConnectionDefinition tempConnection = new ConnectionDefinition();

        if (request.getAttributes() != null) {
            for (Map.Entry<String, String> entry : request.getAttributes().entrySet()) {
                if (entry.getKey().contains("-")) {
                    int dot = entry.getKey().indexOf("-");
                    String entity = entry.getKey().substring(0, dot);
                    String field = entry.getKey().substring(dot + 1);
                    String value = StringUtils.trim(entry.getValue());
                    if (entity.equalsIgnoreCase("device")) {
                        if (field.equalsIgnoreCase("adb")) {
                            tempConnection.setAdb(value);
                        } else if (field.equalsIgnoreCase("ip")) {
                            tempConnection.setIp(value);
                        } else if (field.equalsIgnoreCase("app")) {
                            tempConnection.setApp(value);
                        } else if (field.equalsIgnoreCase("direct")) {
                            tempConnection.setDirect(value);
                        } else if (field.equalsIgnoreCase("helperType")) {
                            tempConnection.setHelperType(ConnectionDefinition.HelperType.valueOf(value));
                        } else if (field.equalsIgnoreCase("adbLevel")) {
                            tempConnection.setAdbLevel(ConnectionDefinition.AdbType.valueOf(value));
                        } else if (field.equalsIgnoreCase("wifi")) {
                            tempConnection.setWifi(Boolean.parseBoolean(value));
                        } else if (field.equalsIgnoreCase("throttle")) {
                            if (StringUtils.isNotBlank(value)) {
                                tempConnection.setThrottle(Integer.parseInt(value));
                            }
                        } else if (field.equalsIgnoreCase("adbPort")) {
                            if (StringUtils.isNotBlank(value)) {
                                tempConnection.setAdbPort(Integer.parseInt(value));
                            }
                        } else if (field.equalsIgnoreCase("helperPort")) {
                            if (StringUtils.isNotBlank(value)) {
                                tempConnection.setHelperPort(Integer.parseInt(value));
                            }
                        }
                    }
                } else {
                    // not defined
                }
            }
        }
        connectionDefinition = tempConnection;
        connectionDefinition.push();
        deviceHelper.setConnectionDefinition(connectionDefinition);
    }

    @POST
    @Path("/process/unload")
    @Produces("application/json")
    public Map<String, String> unloadProcess() {

        checkInitialState();

        StringBuilder sb = new StringBuilder();

        Map<String, String> result = Maps.newHashMap();

        if (runner != null) {
            runner.setStatus(ScriptRunner.Status.PAUSED);
            thread = null;
            runner = null;
            result.put("status", "ok");
        } else {
            result.put("status", "error");
        }

        if (adbWrapper != null) {
            sb.append(adbWrapper.shutdown(connectionDefinition.getAdbLevel()));
            adbWrapper = null;
        }

        result.put("value", sb.toString());

        return result;
    }

    @POST
    @Path("/process/kill")
    @Produces("application/json")
    public Map<String, String> killProcess() {

        checkInitialState();

        StringBuilder sb = new StringBuilder();

        Map<String, String> result = Maps.newHashMap();

        if (runner != null) {
            runner.setStatus(ScriptRunner.Status.PAUSED);
            thread = null;
            runner = null;
            result.put("status", "ok");
        } else {
            result.put("status", "error");
        }

        if (adbWrapper != null) {
            sb.append(adbWrapper.shutdown(connectionDefinition.getAdbLevel()));
            adbWrapper = null;
        }

        result.put("value", sb.toString());

        return result;
    }


    @POST
    @Path("/edit/upload/{type}/{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    public Map<String, String> uploadImageForScreen(@FormDataParam("file") InputStream stream, @FormDataParam("file") FormDataContentDisposition fileDetail, @PathParam("type") String type, @PathParam("id") String id) {
        Map<String, String> result = Maps.newHashMap();
        if (StringUtils.equalsIgnoreCase(type, "SCREEN") || StringUtils.equalsIgnoreCase(type, "COMPONENT")) {
            try {
                BufferedImage bufferedImage = ImageIO.read(stream);
                final File filePath;
                if (StringUtils.equalsIgnoreCase(type, "SCREEN")) {
                    filePath = ScreenDefinition.getPreviewPath(editHolder.getViewDefinition().getViewId(), id);
                } else {
                    filePath = ComponentDefinition.getPreviewPath(editHolder.getViewDefinition().getViewId(), id);
                }
                ImageIO.write(bufferedImage, "PNG", filePath);
                result.put("status", "ok");
            } catch (Exception ex) {
                result.put("status", "error");
            }
        } else {
            result.put("status", "error");
        }
        return result;
    }

    @POST
    @Path("/edit/view")
    @Consumes("application/json")
    @Produces("application/json")
    public PrepResult editView(@RequestBody LoadRequest request) {
        checkInitialState();

        handleConnection(request);

        if (connectionDefinition.getHelperType() == ConnectionDefinition.HelperType.REMOTE) {
            if (!(deviceHelper instanceof RemoteDeviceHelper)) {
                deviceHelper = new RemoteDeviceHelper(connectionDefinition);
            }
        } else if (connectionDefinition.getHelperType() == ConnectionDefinition.HelperType.NOOP) {
            if (!(deviceHelper instanceof NoOpDeviceHelper)) {
                deviceHelper = new NoOpDeviceHelper();
            }
        } else {
            if (!(deviceHelper instanceof LocalDeviceHelper)) {
                deviceHelper = new LocalDeviceHelper(connectionDefinition);
            }
            ((LocalDeviceHelper) deviceHelper).setUsePng(connectionDefinition.getHelperType() == ConnectionDefinition.HelperType.LOCAL_PNG);
        }

        if (adbWrapper != null) {
            adbWrapper.shutdown(connectionDefinition.getAdbLevel());
            adbWrapper = null;
        }

        thread = null;

        frameChoices = new FrameChoices(Constants.ACTION_EDIT, Constants.MODE_VIEW, null, "", request.getDevice(), request.getViews(), request.getScripts());

        if (frameChoices.isValid()) {
            final PrepResult result = new PrepResult(StatusEnum.ok);
            if (runner != null) {
                if (runner.isRunning()) {
                    runner.setStatus(ScriptRunner.Status.PAUSED);
                }
                runner = null;
            }

            setupAdbWrapper(connectionDefinition);

            adbWrapper.connect();

            editHolder = new EditHolder(frameChoices.getScriptEnvironment(), frameChoices.getMapDefinition(), frameChoices.getDeviceDefinition(), frameChoices.getViewDefinition(), connectionDefinition, adbWrapper, deviceHelper);
            deviceHelper = editHolder.getDeviceHelper();
            return result;
        } else {
            return new PrepResult(StatusEnum.error);
        }
    }

    private void setupAdbWrapper(ConnectionDefinition connectionDefinition) {
        if (connectionDefinition.getAdbLevel() == ConnectionDefinition.AdbType.FULL)
            adbKill();

        adbDevices();

        if (connectionDefinition.isWifi()) {
            adbWrapper = new AdbWrapper(connectionDefinition.getIp(), connectionDefinition.getAdbPort());
        } else {
            adbWrapper = new AdbWrapper(connectionDefinition.getDirect());
        }

        adbWrapper.connect();
    }

    private static final ImmutableMap<String, EditActionInterface> ACTIONS = ImmutableMap.<String, EditActionInterface>builder()
            // Screens
            .put("stubScreen", new StubScreenAction())
            .put("verifyScreen", new VerifyScreenAction())
            .put("liveVerifyScreen", new LiveVerifyScreenAction())
            .put("updateScreen", new UpdateScreenAction())
            .put("cacheScreen", new CacheScreenAction())
            .put("editScreen", new EditScreenAction())
            .put("fixScreen", new FixScreenAction())
            .put("repairScreen", new RepairScreenAction())
            // Components
            .put("stubComponent", new StubComponentAction())
            .put("editComponent", new EditComponentAction())
            .put("updateComponent", new UpdateComponentImageAction())
            .build();

    @POST
    @Path("/edit/action/{actionId}/{id}/{value}")
    @Produces("application/json")
    public Map<String, String> editAction(@PathParam("actionId") String actionId, @PathParam("id") String id, @PathParam("value") String value) {
        checkInitialState();
        Map<String, String> result = Maps.newHashMap();
        if (editHolder != null) {
            EditActionInterface editActionInterface = ACTIONS.get(actionId);
            if (editActionInterface == null) {
                result.put("msg", "Unknown Action");
                result.put("status", "error");
            } else {
                result.put("msg", editActionInterface.execute(id, value, editHolder));
                result.put("status", "ok");
            }
        } else {
            result.put("msg", "Edit engine isn't running");
            result.put("status", "error");
        }
        return result;
    }

    @POST
    @Path("/edit/extract/{id}")
    @Produces("application/json")
    public Map<String, String> editAction(@PathParam("id") String id) {
        checkInitialState();
        Map<String, String> result = Maps.newHashMap();
        try {
            if (editHolder != null) {
                ScreenDefinition screenDefinition = editHolder.getScreenForId(id);
                if (screenDefinition == null) {
                    result.put("msg", "Unknown Screen Id: " + id);
                    result.put("status", "error");
                } else {
                    final ObjectMapper objectMapper = JsonTool.getInstance();
                    final String extracted = Base64.getEncoder().encodeToString(objectMapper.writeValueAsString(screenDefinition).getBytes("UTF-8"));
                    result.put("msg", "DONE");
                    result.put("content", extracted);
                    result.put("status", "ok");
                }
            } else {
                result.put("msg", "Edit engine isn't running");
                result.put("status", "error");
            }
        } catch (Exception ex) {
            result.put("msg", ex.getMessage());
            result.put("status", "error");
        }
        return result;
    }

    @POST
    @Path("/edit/import/screen")
    @Produces("application/json")
    public Map<String, String> editImportScreen(@FormParam("content") String content) {
        checkInitialState();
        Map<String, String> result = Maps.newHashMap();
        try {
            if (editHolder != null) {
                final ObjectMapper objectMapper = JsonTool.getInstance();

                byte [] bytes = Base64.getDecoder().decode(content);

                final ScreenDefinition newScreenDef =  objectMapper.readValue(bytes, ScreenDefinition.class);
                final ScreenDefinition oldScreenDefinition = editHolder.getScreenForId(newScreenDef.getScreenId());

                if (oldScreenDefinition == null) {
                    result.put("msg", "The screen to be imported: " + newScreenDef.getScreenId() + " does not exist in the current view definition.");
                    result.put("status", "error");
                } else {

                    oldScreenDefinition.getPoints().clear();
                    oldScreenDefinition.getPoints().addAll(newScreenDef.getPoints());

                    // Save the points
                    editHolder.getViewDefinition().save();

                    result.put("msg", "DONE");
                    result.put("status", "ok");
                }
            } else {
                result.put("msg", "Edit engine isn't running");
                result.put("status", "error");
            }
        } catch (Exception ex) {
            result.put("msg", ex.getMessage());
            result.put("status", "error");
        }
        return result;
    }

    @POST
    @Path("/edit/unload")
    @Produces("application/json")
    public Map<String, String> unloadEdit() {
        checkInitialState();
        Map<String, String> result = Maps.newHashMap();

        if (editHolder != null) {
            editHolder = null;
            result.put("status", "ok");
        } else {
            result.put("status", "error");
        }

        if (adbWrapper != null) {
            adbWrapper.shutdown(connectionDefinition.getAdbLevel());
            adbWrapper = null;
        }

        return result;
    }

    @POST
    @Path("/process/level/console/{level}")
    @Produces("application/json")
    public Map<String, String> setConsoleLevel(@PathParam("level") String level) {
        checkInitialState();
        Loggers.consoleHandler.setLevel(Level.toLevel(level, Level.ERROR));
        Map<String, String> result = Maps.newHashMap();
        result.put("status", "ok");
        return result;
    }

    @POST
    @Path("/process/level/web/{level}")
    @Produces("application/json")
    public Map<String, String> setWebLevel(@PathParam("level") String level) {
        checkInitialState();
        Loggers.webHandler.setLevel(Level.toLevel(level, Level.ERROR));
        Map<String, String> result = Maps.newHashMap();
        result.put("status", "ok");
        return result;
    }

    @POST
    @Path("/process/level/file/{level}")
    @Produces("application/json")
    public Map<String, String> setFileLevel(@PathParam("level") String level) {
        checkInitialState();
        Loggers.fileHandler.setLevel(Level.toLevel(level, Level.ERROR));
        Map<String, String> result = Maps.newHashMap();
        result.put("status", "ok");
        return result;
    }

    @GET
    @Path("/process/info")
    @Consumes("application/json")
    @Produces("application/json")
    public PrepResult infoProcess(@RequestBody Map<String, String> values) {

        checkInitialState();

        if (editHolder != null) {
            return new PrepResult(StatusEnum.ok);
        }

        if (frameChoices != null) {
            final PrepResult result = new PrepResult(StatusEnum.ok);

            final SortedSet<StateDefinition> stateDefinitions = new TreeSet<>(new Comparator<StateDefinition>() {
                @Override
                public int compare(StateDefinition o1, StateDefinition o2) {
                    if (o1.getId().equals("main")) {
                        return -1;
                    } else if (o2.getId().equals("main")) {
                        return 1;
                    }
                    return o1.getName().compareTo(o2.getName());
                }
            });

            Collection<ExecutableLink> executableLinks = frameChoices.getScriptEnvironment().getExecutableStates(ImmutableSet.of(StateType.STATE)).values();
            for (ExecutableLink link : executableLinks) {
                stateDefinitions.add(link.getLink().getState());
            }

            for (StateDefinition definition : stateDefinitions) {
                result.getStates().add(new NamedValueDescriptionItem(definition.getName(), definition.getId(), definition.getDescription()));
            }

            final SortedSet<ComponentDefinition> componentDefinitions = new TreeSet<>(new Comparator<ComponentDefinition>() {
                @Override
                public int compare(ComponentDefinition o1, ComponentDefinition o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            componentDefinitions.addAll(frameChoices.getViewDefinition().getComponents());

            for (ComponentDefinition definition : componentDefinitions) {
                result.getComponents().add(new NamedValueItem(definition.getName(), definition.getComponentId()));
            }

            result.getVariables().addAll(runner.getVariables());
            result.getVariables().sort(new Comparator<VarDefinition>() {
                @Override
                public int compare(VarDefinition o1, VarDefinition o2) {
                    int firstTry = Integer.compare(o1.getOrder(), o2.getOrder());
                    if (firstTry != 0) return firstTry;
                    return o1.getName().compareTo(o2.getName());
                }
            });
            result.getVariableTiers().addAll(runner.getVariableTiers());
            result.getVariableTabs().addAll(runner.getVariableTabs());

            result.setWebLevel(Loggers.webHandler.getLevel().toString());
            result.setConsoleLevel(Loggers.consoleHandler.getLevel().toString());
            result.setFileLevel(Loggers.fileHandler.getLevel().toString());

            result.setViewWidth(frameChoices.getDeviceDefinition().getViewWidth());
            result.setViewHeight(frameChoices.getDeviceDefinition().getViewHeight());
            result.setControlWidth(frameChoices.getDeviceDefinition().getWidth());
            result.setControlHeight(frameChoices.getDeviceDefinition().getHeight());

            return result;
        } else {
            return new PrepResult(StatusEnum.error);
        }
    }

    @GET
    @Path("/edit/view/info")
    @Consumes("application/json")
    @Produces("application/json")
    public PrepResult editViewInfo(@RequestBody Map<String, String> values) {
        checkInitialState();
        if (editHolder != null) {
            PrepResult results = new PrepResult(StatusEnum.ok);

            for (ScreenDefinition screenDefinition : editHolder.getViewDefinition().getScreens()) {
                results.getScreens().add(new NamedValueItem(screenDefinition.getName(), screenDefinition.getScreenId()));
            }
            Collections.sort(results.getScreens());

            for (ComponentDefinition screenDefinition : editHolder.getViewDefinition().getComponents()) {
                results.getComponents().add(new NamedValueItem(screenDefinition.getName(), screenDefinition.getComponentId()));
            }
            Collections.sort(results.getComponents());


            return results;

        } else {
            return new PrepResult(StatusEnum.error);
        }
    }

    @GET
    @Path("/settings/list")
    @Produces("application/json")
    public Map<String, List<String>> listSettings() {
        checkInitialState();

        Map<String, List<String>> values = Maps.newHashMap();
        values.put("devices", Constants.arrayToList(Constants.listJsonFilesIn(new File(Runner.WORKING_DIRECTORY, Constants.PATH_DEVICES), true)));
        values.put("views", Constants.arrayToList(Constants.listFoldersFilesIn(new File(Runner.WORKING_DIRECTORY, Constants.PATH_VIEWS))));
        values.put("scripts", Constants.arrayToList(Constants.listJsonFilesIn(new File(Runner.WORKING_DIRECTORY, Constants.PATH_SCRIPTS), true)));
        values.put("configs", Constants.arrayToList(Constants.listJsonFilesIn(new File(Runner.WORKING_DIRECTORY, Constants.PATH_CONFIGS), false)));
        values.put("states", Constants.arrayToList(Constants.listJsonFilesIn(new File(Runner.WORKING_DIRECTORY, Constants.PATH_STATES), false)));
        return values;
    }

    @GET
    @Path("/files/list")
    @Produces("application/json")
    public FileListResult listFiles() {
        final FileListResult result = new FileListResult();
        final List<String> views = Constants.arrayToList(Constants.listFoldersFilesIn(new File(Runner.WORKING_DIRECTORY, Constants.PATH_VIEWS)));
        final List<String> scripts = Constants.arrayToList(Constants.listJsonFilesIn(new File(Runner.WORKING_DIRECTORY, Constants.PATH_SCRIPTS), false));
        final List<String> configs = Constants.arrayToList(Constants.listJsonFilesIn(new File(Runner.WORKING_DIRECTORY, Constants.PATH_CONFIGS), false));
        final List<String> states = Constants.arrayToList(Constants.listJsonFilesIn(new File(Runner.WORKING_DIRECTORY, Constants.PATH_STATES), false));
        result.getViews().addAll(views);
        result.getScripts().addAll(scripts);
        result.getConfigs().addAll(configs);
        result.getScripts().addAll(states);
        return result;
    }

    @POST
    @Path("/control/key/event/{event}")
    @Produces("application/json")
    public ValueResult controlKeyEvent(@PathParam("event") String eventId) {
        checkInitialState();
        ValueResult result = new ValueResult();
        AdbUtils.event(eventId, false, adbWrapper, false);
        result.setStatus("ok");
        return result;
    }

    @POST
    @Path("/control/tap/{x}/{y}")
    @Produces("application/json")
    public ValueResult controlKeyEvent(@PathParam("x") int x, @PathParam("y") int y) {
        checkInitialState();
        ValueResult result = new ValueResult();
        AdbUtils.tap(x, y, adbWrapper);
        result.setStatus("ok");
        return result;
    }

    @POST
    @Path("/control/component/{componentId}/{actionId}")
    @Produces("application/json")
    public ValueResult controlKeyEvent(@PathParam("componentId") String componentId, @PathParam("actionId") String actionId) {
        checkInitialState();
        ValueResult result = new ValueResult();

        ComponentDefinition componentDefinition = null;

        for (ComponentDefinition temp : frameChoices.getViewDefinition().getComponents()) {
            if (temp.getComponentId().equals(componentId)) {
                componentDefinition = temp;
                break;
            }
        }

        if (componentDefinition == null) {
            result.setStatus("fail");
            result.setValue("Component missing");
            return result;
        }

        ActionType type = ActionType.valueOf(actionId);

        AdbUtils.component(frameChoices.getDeviceDefinition(), componentDefinition, type, adbWrapper, false);

        result.setStatus("ok");
        return result;
    }

    @GET
    @Path("/configs")
    @Produces("application/json")
    public ConfigListResponse listConfigs() {
        final File configPath = new File(Runner.WORKING_DIRECTORY, Constants.PATH_CONFIGS);
        final ObjectMapper objectMapper = JsonTool.getInstance();
        final List<LoadRequest> result = Lists.newArrayList();
        final List<String> configNames = Constants.arrayToList(Constants.listJsonFilesIn(configPath, false));

        for (String configName : configNames) {
            if (StringUtils.isBlank(configName)) continue;
            File configFile = new File(configPath, configName + ".json");
            try {
                LoadRequest loadRequest = objectMapper.readValue(configFile, LoadRequest.class);
                if (StringUtils.isBlank(loadRequest.getConfigName())) {
                    loadRequest.setConfigName(configName);
                }
                if (StringUtils.isBlank(loadRequest.getStateName())) {
                    loadRequest.setStateName(loadRequest.getConfigName());
                }
                result.add(loadRequest);
            } catch (Exception ex) {
                logger.error(configName + ": " + ex.getMessage());
            }
        }
        result.sort(new Comparator<LoadRequest>() {
            @Override
            public int compare(LoadRequest o1, LoadRequest o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
        return new ConfigListResponse(result);
    }

    @POST
    @Path("/configs")
    @Produces("application/json")
    public Map<String, String> saveConfigs(@RequestBody LoadRequest request) {
        final File configPath = new File(Runner.WORKING_DIRECTORY, Constants.PATH_CONFIGS);
        if (StringUtils.isBlank(request.getStateName())) {
            return errorResponse("StateName was blank, save canceled");
        } else if (StringUtils.isBlank(request.getConfigName())) {
            return errorResponse("Config Name was blank, save canceled");
        }
        if (!Constants.ID_PATTERN.matcher(request.getStateName()).matches()) {
            return errorResponse("StateName was not formatted correctly, save canceled");
        } else if (!Constants.ID_PATTERN.matcher(request.getConfigName()).matches()) {
            return errorResponse("ConfigName was not formatted correctly, save canceled");
        }
        final ObjectMapper objectMapper = JsonTool.getInstance();
        final File configFile = new File(configPath, request.getConfigName() + ".json");
        try {
            objectMapper.writeValue(configFile, request);
            return okResponse("File saved");
        } catch (Exception ex) {
            logger.error("Save Failed: " + ex.getMessage());
            return errorResponse(ex.getMessage());
        }
    }

    @POST
    @Path("/configs/download")
    public Response downloadConfig(@FormParam("name") String fileName) {
        final File configPath = new File(Runner.WORKING_DIRECTORY, Constants.PATH_CONFIGS);
        final File configFile = new File(configPath, fileName + ".json");
        final ObjectMapper objectMapper = JsonTool.getInstance();
        try {
            LoadRequest data = objectMapper.readValue(configFile, LoadRequest.class);
            return Response.status(200).entity(data).type(MediaType.APPLICATION_JSON).header("Content-disposition", "attachment; filename=" + fileName + ".json").build();
        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }

    @POST
    @Path("/states/download")
    public Response downloadState(@FormParam("name") String fileName) {
        final File configPath = new File(Runner.WORKING_DIRECTORY, Constants.PATH_STATES);
        final File configFile = new File(configPath, fileName + ".json");
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(configFile);
            char[] temp = new char[128];
            int len = 0;
            StringBuilder sb = new StringBuilder();
            while ((len = fileReader.read(temp, 0, temp.length)) > 0) {
                sb.append(temp, 0, len);
            }
            return Response.status(200).entity(sb.toString()).type(MediaType.APPLICATION_JSON).header("Content-disposition", "attachment; filename=" + fileName + ".json").build();
        } catch (Exception ex) {
            return Response.status(500).build();
        } finally {
            Closer.close(fileReader);
        }
    }

    @DELETE
    @Path("/configs/{configName}")
    @Produces("application/json")
    public Map<String, String> saveConfigs(@PathParam("configName") String configName) {
        final File configPath = new File(Runner.WORKING_DIRECTORY, Constants.PATH_CONFIGS);
        if (StringUtils.isBlank(configName)) {
            return errorResponse("ConfigName was blank, delete canceled");
        }
        if (!Constants.ID_PATTERN.matcher(configName).matches()) {
            return errorResponse("ConfigName was not formatted correctly, delete canceled");
        }
        final File configFile = new File(configPath, configName + ".json");
        try {
            if (configFile.exists()) {
                if (configFile.delete()) {
                    return okResponse("File deleted");
                } else {
                    return errorResponse("Could not delete file");
                }
            } else {
                return errorResponse("File not found");
            }
        } catch (Exception ex) {
            logger.error("Delete Failed: " + ex.getMessage());
            return errorResponse(ex.getMessage());
        }
    }

    private Map<String, String> errorResponse(String msg) {
        return ImmutableMap.of("status", "error", "msg", msg);
    }

    private Map<String, String> okResponse(String msg) {
        return ImmutableMap.of("status", "ok", "msg", msg);
    }

    private static final int previewFactor = 8;

    @GET
    @Path("/screen")
    @Produces("image/png")
    public Response screen(@QueryParam("factor") String factor, @QueryParam("live") String isLive) {
        try {
            int factorValue = previewFactor;
            if (StringUtils.isNotBlank(factor)) {
                factorValue = Integer.parseInt(factor);
            }

            if ("true".equals(isLive)) {
                if (frameChoices != null && deviceHelper instanceof LocalDeviceHelper) {
                    // Get the Image
                    screenWrapper = deviceHelper.download();
                }
            }

            if (screenWrapper != null) {
                if (screenWrapper.isReady()) {

                    int newHeight = screenWrapper.getHeight() / factorValue;
                    int newWidth = screenWrapper.getWidth() / factorValue;

                    BufferedImage bufferedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

                    Sampler sampler = new Sampler();
                    for (int y = 0; y < newHeight; y++) {
                        for (int x = 0; x < newWidth; x++) {
                            screenWrapper.getPixel(x * factorValue, y * factorValue, sampler);
                            bufferedImage.setRGB(x, y, (sampler.getR() << 16) + (sampler.getG() << 8) + sampler.getB());
                        }
                    }

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    try {
                        ImageIO.write(bufferedImage, "PNG", byteArrayOutputStream);
                        return Response.status(200).header("content-type", "image/png").entity(byteArrayOutputStream.toByteArray()).build();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Response.status(500).build();
    }

    @POST
    @Path("/screen/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response screenDown() {
        try {
            if (screenWrapper != null) {
                if (screenWrapper.isReady()) {
                    byte[] stream = screenWrapper.outputPng();
                    if (stream != null) {
                        return Response.ok(stream, MediaType.APPLICATION_OCTET_STREAM).header("content-disposition", "attachment; filename = screen.png").build();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Response.status(500).build();
    }

    private static ImageWrapper screenWrapper;

    @POST
    @Path("/screen/prep")
    @Produces("application/json")
    public ValueResult screenPrep() {
        try {
            checkInitialState();
            if (frameChoices != null) {
                // Save the Image
                deviceHelper.refresh(adbWrapper);
                // Get the Image
                screenWrapper = deviceHelper.download();
                return new ValueResult().setStatus("ok");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ValueResult().setStatus("fail");
    }

    @POST
    @Path("/screen/prep/cache")
    @Produces("application/json")
    public ValueResult cacheScreenPrep() {
        try {
            checkInitialState();
            if (frameChoices != null) {
                if (deviceHelper instanceof LocalDeviceHelper) {
                    screenWrapper = deviceHelper.download();
                    if (screenWrapper != null) {
                        return new ValueResult().setStatus("ok");
                    }
                }
                return new ValueResult().setStatus("fail");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ValueResult().setStatus("fail");
    }

    @GET
    @Path("/resource/{filename}")
    public Response resource(@PathParam("filename") String path) {
        checkInitialState();

        URL url = Resources.getResource("web/resources/" + path);
        String contentType;
        if (path.toLowerCase().endsWith(".png")) {
            contentType = "image/png";
        } else if (path.toLowerCase().endsWith(".svg")) {
            contentType = "image/svg";
        } else if (path.toLowerCase().endsWith(".ico")) {
            contentType = "image/x-icon";
        } else if (path.toLowerCase().endsWith(".xml")) {
            contentType = "text/xml";
        } else if (path.toLowerCase().endsWith(".js")) {
            contentType = "application/javascript";
        } else if (path.toLowerCase().endsWith(".css")) {
            contentType = "text/css";
        } else if (path.toLowerCase().endsWith(".woff")) {
            contentType = "font/woff";
        } else if (path.toLowerCase().endsWith(".woff2")) {
            contentType = "font/woff2";
        } else if (path.toLowerCase().endsWith(".eot")) {
            contentType = "font/eot";
        } else if (path.toLowerCase().endsWith(".ttf")) {
            contentType = "font/ttf";
        } else if (path.toLowerCase().endsWith(".webmanifest")) {
            contentType = "application/manifest+json";
        } else {
            return Response.status(404).build();
        }
        try {
            return Response.status(200).header("content-type", contentType).entity(Resources.toByteArray(url)).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(500).build();
        }
    }
}
