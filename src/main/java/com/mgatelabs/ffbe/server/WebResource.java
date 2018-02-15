package com.mgatelabs.ffbe.server;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.mgatelabs.ffbe.runners.ScriptRunner;
import com.mgatelabs.ffbe.shared.details.ConnectionDefinition;
import com.mgatelabs.ffbe.shared.details.PlayerDefinition;
import com.mgatelabs.ffbe.shared.details.StateDefinition;
import com.mgatelabs.ffbe.shared.helper.DeviceHelper;
import com.mgatelabs.ffbe.ui.FrameChoices;
import com.mgatelabs.ffbe.ui.frame.StartupFrame;
import com.mgatelabs.ffbe.ui.panels.LogPanel;
import com.mgatelabs.ffbe.ui.panels.RunScriptPanel;
import com.mgatelabs.ffbe.ui.utils.Constants;
import com.mgatelabs.ffbe.ui.utils.CustomHandler;
import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.LogRecord;

/**
 * Created by @mgatelabs (Michael Fuller) on 2/13/2018.
 */
@Path("/ffbe")
public class WebResource {

    // This can change
    private static ScriptRunner runner;
    private static RunScriptPanel.ScriptThread thread;

    // These don't change
    private static ConnectionDefinition connectionDefinition;
    private static DeviceHelper deviceHelper;
    private static PlayerDefinition playerDefinition;
    private static CustomHandler handler;

    private synchronized boolean checkInitialState() {
        if (connectionDefinition == null) {
            playerDefinition = PlayerDefinition.read();
            connectionDefinition = ConnectionDefinition.read();
            deviceHelper = new DeviceHelper(connectionDefinition.getIp());
            handler = new CustomHandler();
            return true;
        }
        return false;
    }

    @GET
    @Path("/")
    @Produces("text/html")
    public String control() {

        URL url = Resources.getResource("pages/index.html");

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

    @GET
    @Path("/device/ip")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ValueResult getDeviceIp() {
        checkInitialState();
        final ValueResult valueResult = new ValueResult();
        valueResult.setStatus("OK");
        valueResult.setValue(connectionDefinition.getIp());
        return valueResult;
    }

    @POST
    @Path("/device/ip")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized void setDeviceIp(@FormParam("value") String value) {
        checkInitialState();
        connectionDefinition.setIp(value);
        connectionDefinition.write();
    }

    @GET
    @Path("/status")
    @Produces("application/json")
    public StatusResult status() {
        StatusResult result = new StatusResult();

        if (checkInitialState()) {
            result.setStatus(StatusResult.Status.INIT);
        } else {
            if (runner == null) {
                result.setStatus(StatusResult.Status.INIT);
            } else {
                result.setState(runner.getCurrentStateId());
                if (thread == null) {
                    result.setStatus(StatusResult.Status.READY);
                } else {
                    if (runner.isRunning()) {
                        result.setStatus(StatusResult.Status.RUNNING);
                    } else {
                        result.setStatus(StatusResult.Status.STOPPED);
                        thread = null;
                    }
                }
            }
        }

        final ImmutableList<LogRecord> records = handler.getEvents();

        for (LogRecord record : records) {

            String sourceName;
            if (record.getSourceClassName() != null && record.getSourceClassName().lastIndexOf('.') > 0) {
                sourceName = record.getSourceClassName().substring(record.getSourceClassName().lastIndexOf('.'));
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
    @Path("/process/playPause")
    @Produces("application/json")
    public Map<String, String> prepProcess(@FormParam("stateId") String stateId) {
        Map<String, String> result = Maps.newHashMap();

        if (runner != null) {
            if (thread == null) {
                thread = new RunScriptPanel.ScriptThread(runner, stateId);
                thread.start();
            } else {
                runner.setStatus(ScriptRunner.Status.PAUSED);
                thread = null;
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
    public Map<String, String> prepProcess(@RequestBody Map<String, String> values) {
        Map<String, String> result = Maps.newHashMap();

        FrameChoices frameChoices = new FrameChoices(Constants.ACTION_RUN, Constants.MODE_SCRIPT, playerDefinition, "", values.get("script"), values.get("script2"), values.get("device"), values.get("view"), values.get("view2"));

        if (frameChoices.isValid()) {
            result.put("status", "true");
            runner = new ScriptRunner(playerDefinition, deviceHelper, frameChoices.getScriptDefinition(), frameChoices.getDeviceDefinition(), frameChoices.getViewDefinition(), handler);

            SortedSet<StateDefinition> definitions = new TreeSet<>(new Comparator<StateDefinition>() {
                @Override
                public int compare(StateDefinition o1, StateDefinition o2) {
                    if (o1.getId().equals("main")) {
                        return -1;
                    } else if (o2.getId().equals("main")) {
                        return 1;
                    }
                    return o1.getId().compareTo(o2.getId());
                }
            });

            definitions.addAll(frameChoices.getScriptDefinition().getFilteredStates().values());

            List<String> stateValues = Lists.newArrayList();
            List<String> stateNames = Lists.newArrayList();

            for (StateDefinition definition: definitions) {
                stateValues.add(definition.getId());
                stateNames.add(definition.getName());
            }

            result.put("stateValues", Joiner.on("^").join(stateValues).toString());
            result.put("stateNames", Joiner.on("^").join(stateNames).toString());

        } else {
            result.put("status", "false");
        }

        return result;
    }

    @GET
    @Path("/settings/list")
    @Produces("application/json")
    public Map<String, List<String>> listSettings() {
        Map<String, List<String>> values = Maps.newHashMap();
        values.put("devices", StartupFrame.arrayToList(StartupFrame.listJsonFilesIn(new File(StartupFrame.PATH_DEVICES))));
        values.put("views", StartupFrame.arrayToList(StartupFrame.listFoldersFilesIn(new File(StartupFrame.PATH_VIEWS))));
        values.put("scripts", StartupFrame.arrayToList(StartupFrame.listJsonFilesIn(new File(StartupFrame.PATH_SCRIPTS))));
        return values;
    }

    @GET
    @Path("/resource/{filename}")
    public Response resource(@PathParam("filename") String path) {
        URL url = Resources.getResource(path);
        String contentType;
        if (path.toLowerCase().endsWith(".png")) {
            contentType = "image/png";
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
