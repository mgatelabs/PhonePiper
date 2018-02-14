package com.mgatelabs.ffbe.server;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.mgatelabs.ffbe.runners.ScriptRunner;
import com.mgatelabs.ffbe.shared.details.ConnectionDefinition;
import com.mgatelabs.ffbe.shared.details.PlayerDefinition;
import com.mgatelabs.ffbe.shared.helper.DeviceHelper;
import com.mgatelabs.ffbe.ui.FrameChoices;
import com.mgatelabs.ffbe.ui.frame.StartupFrame;
import com.mgatelabs.ffbe.ui.utils.Constants;
import com.mgatelabs.ffbe.ui.utils.CustomHandler;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by @mgatelabs (Michael Fuller) on 2/13/2018.
 */
@Path("/ffbe")
public class WebResource {

    // This can change
    private static ScriptRunner runner;

    // These don't change
    private static final DeviceHelper deviceHelper;
    private static final PlayerDefinition playerDefinition;
    private static final CustomHandler handler;

    static {
        playerDefinition = PlayerDefinition.read();
        ConnectionDefinition connectionDefinition = ConnectionDefinition.read();
        deviceHelper = new DeviceHelper(connectionDefinition.getIp());
        handler = new CustomHandler();
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

    @POST
    @Path("/process/prep")
    @Produces("application/json")
    public Map<String, String> prepProcess(@RequestBody Map<String, String> values) {
        Map<String, String> result = Maps.newHashMap();

        FrameChoices frameChoices = new FrameChoices(Constants.ACTION_RUN, Constants.MODE_SCRIPT, playerDefinition, "", values.get("script"), values.get("script2"), values.get("device"), values.get("view"), values.get("view2"));

        if (frameChoices.isValid()) {
            result.put("status", "true");
            runner = new ScriptRunner(playerDefinition, deviceHelper, frameChoices.getScriptDefinition(), frameChoices.getDeviceDefinition(), frameChoices.getViewDefinition(), handler);
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
