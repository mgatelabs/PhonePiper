package com.mgatelabs.piper.server;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.mgatelabs.piper.Runner;
import com.mgatelabs.piper.runners.ScriptRunner;
import com.mgatelabs.piper.server.actions.*;
import com.mgatelabs.piper.server.entities.*;
import com.mgatelabs.piper.shared.details.*;
import com.mgatelabs.piper.shared.helper.DeviceHelper;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.util.AdbShell;
import com.mgatelabs.piper.shared.util.AdbUtils;
import com.mgatelabs.piper.shared.util.Loggers;
import com.mgatelabs.piper.ui.FrameChoices;
import com.mgatelabs.piper.ui.frame.StartupFrame;
import com.mgatelabs.piper.ui.panels.LogPanel;
import com.mgatelabs.piper.ui.panels.RunScriptPanel;
import com.mgatelabs.piper.ui.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 2/13/2018.
 */
@Path("/piper")
public class WebResource {

    // This can change
    private static ScriptRunner runner;
    private static RunScriptPanel.ScriptThread thread;

    private static EditHolder editHolder;

    // These don't change
    private static ConnectionDefinition connectionDefinition;
    private static FrameChoices frameChoices;
    private static DeviceHelper deviceHelper;
    private static PlayerDefinition playerDefinition;

    private Logger logger = Logger.getLogger("WebResource");

    private synchronized boolean checkInitialState() {
        if (connectionDefinition == null) {
            playerDefinition = PlayerDefinition.read();
            connectionDefinition = new ConnectionDefinition();
            deviceHelper = new DeviceHelper(connectionDefinition.getIp());
            Loggers.webLogger.setLevel(Level.INFO);
            Loggers.fileLogger.setLevel(Level.INFO);
            logger.addHandler(Loggers.webLogger);
            logger.setLevel(Level.FINEST);
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

    @GET
    @Path("/player/level")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ValueResult getPlayerLevel() {
        checkInitialState();
        final ValueResult valueResult = new ValueResult();
        valueResult.setStatus("OK");
        valueResult.setValue(Integer.toString(playerDefinition.getLevel()));
        return valueResult;
    }

    @POST
    @Path("/player/level")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized void setPlayerLevel(@FormParam("value") int level) {
        checkInitialState();
        playerDefinition.setLevel(level);
        playerDefinition.write();
    }

    @POST
    @Path("/variable")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized void setDeviceIp(@FormParam("key") String key, @FormParam("value") String value) {
        checkInitialState();
        if (runner != null) {
            runner.updateVariable(key, value);

            VarStateDefinition varStateDefinition = new VarStateDefinition();
            for (VarDefinition varDefinition : runner.getVariables()) {
                if (varDefinition.getModify() == VarModify.EDITABLE) {
                    varStateDefinition.addItem(varDefinition);
                }
            }
            if (varStateDefinition.getItems().size() > 0) {
                varStateDefinition.save(frameChoices.getScriptName());
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
            valueResult.setStatus("OK");
        } catch (Exception ex) {
            valueResult.setStatus("FAIL");
        }
        return valueResult;
    }

    @POST
    @Path("/adb/usb")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ValueResult adbUseUSB() {
        checkInitialState();
        final ValueResult valueResult = new ValueResult();
        try {
            if (runner != null) {
                runner.stopShell();
            }
            String s = AdbShell.enableUsb();
            if (runner != null) {
                runner.restartShell();
            }
            valueResult.setValue(s);
            valueResult.setStatus("OK");
        } catch (Exception ex) {
            valueResult.setStatus("FAIL");
        }
        return valueResult;
    }

    @POST
    @Path("/adb/restart")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ValueResult adbRestart() {
        checkInitialState();
        final ValueResult valueResult = new ValueResult();
        if (runner != null) {
            try {
                runner.restartShell();
                valueResult.setStatus("OK");
            } catch (Exception ex) {
                valueResult.setStatus("FAIL");
                ex.printStackTrace();
            }
        } else if (editHolder != null) {
            try {
                editHolder.restartShell();
                valueResult.setStatus("OK");
            } catch (Exception ex) {
                valueResult.setStatus("FAIL");
                ex.printStackTrace();
            }
        } else {
            valueResult.setStatus("FAIL");
        }
        return valueResult;
    }

    @POST
    @Path("/adb/kill")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ValueResult adbKill() {
        checkInitialState();
        final ValueResult valueResult = new ValueResult();
        if (runner != null) {
            try {
                if (runner != null) {
                    runner.stopShell();
                }
                String s = AdbShell.killServer();
                if (runner != null) {
                    runner.restartShell();
                }
                valueResult.setValue(s);
                valueResult.setStatus("OK");
            } catch (Exception ex) {
                valueResult.setStatus("FAIL");
                ex.printStackTrace();
            }
        } else {
            valueResult.setStatus("FAIL");
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
            if (runner != null) {
                runner.stopShell();
            }
            String s = AdbShell.enableRemote();
            if (runner != null) {
                runner.restartShell();
            }
            valueResult.setValue(s);
            valueResult.setStatus("OK");
        } catch (Exception ex) {
            valueResult.setStatus("FAIL");
            valueResult.setValue(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        return valueResult;
    }

    @POST
    @Path("/adb/connect")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ValueResult adbConnectRemote() {
        checkInitialState();
        final ValueResult valueResult = new ValueResult();
        try {
            if (runner != null) {
                runner.stopShell();
            }

            String s = AdbShell.connect(deviceHelper.getIpAddress());
            if (runner != null) {
                runner.restartShell();
            }
            valueResult.setValue(s);
            valueResult.setStatus("OK");
        } catch (Exception ex) {
            valueResult.setStatus("FAIL");
            valueResult.setValue(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        return valueResult;
    }

    @POST
    @Path("/adb/devices")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ValueResult adbDevices() {
        checkInitialState();
        final ValueResult valueResult = new ValueResult();
        try {
            String s = AdbShell.devices();
            valueResult.setValue(s);
            valueResult.setStatus("OK");
        } catch (Exception ex) {
            valueResult.setStatus("FAIL");
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

        final ImmutableList<LogRecord> records = Loggers.webLogger.getEvents();

        for (LogRecord record : records) {

            String sourceName;
            if (record.getSourceClassName() != null && record.getSourceClassName().lastIndexOf('.') > 0) {
                sourceName = record.getSourceClassName().substring(record.getSourceClassName().lastIndexOf('.') + 1);
            } else if (record.getLoggerName() != null) {
                sourceName = record.getLoggerName();
            } else {
                sourceName = "Unknown";
            }

            result.getLogs().add(
                    new StatusLog(
                            sourceName,
                            LogPanel.sdf.format(new Date(record.getMillis())),
                            record.getLevel().getName(),
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
                thread = new RunScriptPanel.ScriptThread(runner, stateId);
                thread.start();
            } else {
                runner.setStatus(ScriptRunner.Status.PAUSED);
            }
            result.put("status", "true");
        } else {
            result.put("status", "false");
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

        thread = null;
        List<String> views = Lists.newArrayList();
        views.addAll(request.getViews());

        List<String> scripts = Lists.newArrayList();
        scripts.addAll(request.getScripts());

        frameChoices = new FrameChoices(Constants.ACTION_RUN, Constants.MODE_SCRIPT, playerDefinition, "", request.getDevice(), views, scripts);

        if (frameChoices.isValid()) {
            final PrepResult result = new PrepResult(StatusEnum.OK);

            editHolder = null;

            runner = new ScriptRunner(playerDefinition, connectionDefinition, deviceHelper, frameChoices.getScriptDefinition(), frameChoices.getDeviceDefinition(), frameChoices.getViewDefinition(), Loggers.webLogger, Loggers.fileLogger);

            if (VarStateDefinition.exists(frameChoices.getScriptName())) {
                VarStateDefinition varStateDefinition = VarStateDefinition.read(frameChoices.getScriptName());
                for (VarDefinition varDefinition : varStateDefinition.getItems()) {
                    runner.updateVariable(varDefinition.getName(), varDefinition.getValue());
                }
            }

            return result;

        } else {
            return new PrepResult(StatusEnum.FAIL);
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
                        } else if (field.equalsIgnoreCase("direct")) {
                            tempConnection.setDirect(value);
                        } else if (field.equalsIgnoreCase("wifi")) {
                            tempConnection.setWifi(Boolean.parseBoolean(value));
                        }
                    }
                } else {
                    // not defined
                }
            }
        }
        connectionDefinition = tempConnection;
        connectionDefinition.push();
        deviceHelper.setIpAddress(connectionDefinition.getIp());
    }

    @POST
    @Path("/process/unload")
    @Produces("application/json")
    public Map<String, String> unloadProcess() {

        checkInitialState();

        Map<String, String> result = Maps.newHashMap();

        if (runner != null) {
            runner.setStatus(ScriptRunner.Status.PAUSED);
            thread = null;
            runner = null;
            result.put("status", "true");
        } else {
            result.put("status", "false");
        }

        return result;
    }

    @POST
    @Path("/process/kill")
    @Produces("application/json")
    public Map<String, String> killProcess() {

        checkInitialState();

        Map<String, String> result = Maps.newHashMap();

        if (runner != null) {
            runner.setStatus(ScriptRunner.Status.PAUSED);
            thread = null;
            runner = null;
            result.put("status", "true");
        } else {
            result.put("status", "false");
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

        thread = null;

        frameChoices = new FrameChoices(Constants.ACTION_EDIT, Constants.MODE_VIEW, playerDefinition, "", request.getDevice(), request.getViews(), request.getScripts());

        if (frameChoices.isValid()) {
            final PrepResult result = new PrepResult(StatusEnum.OK);
            if (runner != null) {
                if (runner.isRunning()) {
                    runner.setStatus(ScriptRunner.Status.PAUSED);
                }
                runner = null;
            }
            editHolder = new EditHolder(frameChoices.getScriptDefinition(), frameChoices.getMapDefinition(), frameChoices.getDeviceDefinition(), frameChoices.getViewDefinition(), connectionDefinition, new AdbShell(frameChoices.getDeviceDefinition()), deviceHelper);
            deviceHelper = editHolder.getDeviceHelper();
            return result;
        } else {
            return new PrepResult(StatusEnum.FAIL);
        }
    }

    private static final ImmutableMap<String, EditActionInterface> ACTIONS = ImmutableMap.<String, EditActionInterface>builder()
            // Screens
            .put("stubScreen", new StubScreenAction())
            .put("verifyScreen", new VerifyScreenAction())
            .put("liveVerifyScreen", new LiveVerifyScreenAction())
            .put("updateScreen", new UpdateScreenAction())
            .put("editScreen", new EditScreenAction())
            .put("fixScreen", new FixScreenAction())
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
                result.put("status", "false");
            } else {
                result.put("msg", editActionInterface.execute(id, value, editHolder, logger));
                result.put("status", "true");
            }
        } else {
            result.put("msg", "Edit engine isn't running");
            result.put("status", "false");
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
            result.put("status", "true");
        } else {
            result.put("status", "false");
        }
        return result;
    }

    @POST
    @Path("/process/level/web/{level}")
    @Produces("application/json")
    public Map<String, String> setWebLevel(@PathParam("level") String level) {
        checkInitialState();
        updateLoggerFor(Loggers.webLogger, java.util.logging.Level.parse(level));
        Map<String, String> result = Maps.newHashMap();
        result.put("status", "true");
        return result;
    }

    @POST
    @Path("/process/level/file/{level}")
    @Produces("application/json")
    public Map<String, String> setFileLevel(@PathParam("level") String level) {
        checkInitialState();
        updateLoggerFor(Loggers.fileLogger, java.util.logging.Level.parse(level));
        Map<String, String> result = Maps.newHashMap();
        result.put("status", "true");
        return result;
    }

    private void updateLoggerFor(Handler handler, Level level) {
        handler.setLevel(level);
        if (runner != null) {
            runner.updateLogger(Loggers.webLogger.getLevel().intValue() < Loggers.fileLogger.getLevel().intValue() ? Loggers.webLogger.getLevel() : Loggers.fileLogger.getLevel());
        }
    }

    @GET
    @Path("/process/info")
    @Consumes("application/json")
    @Produces("application/json")
    public PrepResult infoProcess(@RequestBody Map<String, String> values) {

        checkInitialState();

        if (editHolder != null) {
            return new PrepResult(StatusEnum.OK);
        }

        if (frameChoices != null) {
            final PrepResult result = new PrepResult(StatusEnum.OK);

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

            stateDefinitions.addAll(frameChoices.getScriptDefinition().getFilteredStates().values());

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

            result.setWebLevel(Loggers.webLogger.getLevel().getName());
            result.setFileLevel(Loggers.fileLogger.getLevel().getName());

            return result;
        } else {
            return new PrepResult(StatusEnum.FAIL);
        }
    }

    @GET
    @Path("/edit/view/info")
    @Consumes("application/json")
    @Produces("application/json")
    public PrepResult editViewInfo(@RequestBody Map<String, String> values) {
        checkInitialState();
        if (editHolder != null) {
            PrepResult results = new PrepResult(StatusEnum.OK);

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
            return new PrepResult(StatusEnum.FAIL);
        }
    }

    @GET
    @Path("/settings/list")
    @Produces("application/json")
    public Map<String, List<String>> listSettings() {
        checkInitialState();

        Map<String, List<String>> values = Maps.newHashMap();
        values.put("devices", StartupFrame.arrayToList(StartupFrame.listJsonFilesIn(new File(Runner.WORKING_DIRECTORY, StartupFrame.PATH_DEVICES))));
        values.put("views", StartupFrame.arrayToList(StartupFrame.listFoldersFilesIn(new File(Runner.WORKING_DIRECTORY, StartupFrame.PATH_VIEWS))));
        values.put("scripts", StartupFrame.arrayToList(StartupFrame.listJsonFilesIn(new File(Runner.WORKING_DIRECTORY, StartupFrame.PATH_SCRIPTS))));
        return values;
    }

    @GET
    @Path("/screen")
    public Response screen() {
        try {
            checkInitialState();
            if (frameChoices != null) {
                // Save the Image
                AdbUtils.persistScreen(new AdbShell(frameChoices.getDeviceDefinition()));
                // Get the Image
                ImageWrapper wrapper = deviceHelper.download();

                if (wrapper.isReady()) {
                    byte[] stream = wrapper.outputPng();
                    if (stream != null) {
                        return Response.status(200).header("content-type", "image/png").entity(stream).build();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Response.status(500).build();
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
