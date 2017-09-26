package com.mgatelabs.ffbe.ui.frame;

import com.mgatelabs.ffbe.shared.details.*;
import com.mgatelabs.ffbe.shared.mapper.MapDefinition;
import com.mgatelabs.ffbe.shared.util.AdbShell;
import com.mgatelabs.ffbe.ui.FrameChoices;
import com.mgatelabs.ffbe.ui.panels.*;
import com.mgatelabs.ffbe.ui.utils.CustomHandler;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/12/2017.
 */
public class MainFrame extends JFrame {

    final PlayerDefinition playerDefinition;
    private ScriptDefinition scriptDefinition;
    private MapDefinition mapDefinition;
    private DeviceDefinition deviceDefinition;
    private ViewDefinition viewDefinition;
    private ConnectionDefinition connectionDefinition;
    private RunScriptPanel runScriptPanel;
    private AdbShell shell;

    private JDesktopPane desktopPane;
    private PlayerPanel playerPanel;

    private MapPanel mapPanel;
    private MapperPanel mapperPanel;
    private ConnectionPanel connectionPanel;

    private LogPanel logPanel;

    private CustomHandler customHandler;

    public MainFrame(FrameChoices choices) throws HeadlessException {
        super("FFBExecute 0.0.1");
        setMinimumSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(1024, 768));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        customHandler = new CustomHandler();

        shell = new AdbShell();

        connectionDefinition = ConnectionDefinition.read();

        playerDefinition = choices.getPlayerDefinition();
        scriptDefinition = choices.getScriptDefinition();
        mapDefinition = choices.getMapDefinition();
        deviceDefinition = choices.getDeviceDefinition();
        viewDefinition = choices.getViewDefinition();

        boolean showMap = false;
        boolean showPlayer = false;
        boolean showStates = false;
        boolean showMapper = false;
        boolean showAdb = false;
        boolean showConnection =  false;
        boolean showRunScript =  false;

        switch (choices.getAction()) {
            case RUN: {
                // Make sure the view is ready
                viewDefinition = ViewDefinition.read(deviceDefinition.getViewId());

                showStates = true;
                showMap = true;
                showAdb = true;
                showConnection = true;
                showRunScript = true;

            } break;
            case EDIT: {
                switch (choices.getMode()) {
                    case SCRIPT: {

                    } break;
                    case MAP: {
                        showMap = true;
                    } break;
                    case VIEW: {
                        showConnection = true;
                    } break;
                    case DEVICE: {

                    } break;
                }
            } break;
            case DELETE: {

            } break;
            case CREATE: {
                switch (choices.getMode()) {
                    case MAP: {
                        mapDefinition = new MapDefinition();
                        mapDefinition.addFloor("start");
                        showMap = true;
                        showMapper = true;
                        showAdb = true;
                        showConnection = true;
                    } break;
                }
            } break;
        }

        desktopPane = new JDesktopPane();

        logPanel = new LogPanel(customHandler);
        desktopPane.add(logPanel);

        try {
            logPanel.setIcon(true);
         } catch (PropertyVetoException e) {
            e.printStackTrace();
         }

        // playerPanel = new PlayerPanel(playerDetail);
        // desktopPane.add(playerPanel);

        int column0Top = 0;

        if (showMap) {
            mapPanel = new MapPanel();
            mapPanel.setMap(mapDefinition);
            mapPanel.setLocation(300, 0);
            desktopPane.add(mapPanel);
            mapPanel.setMap(mapDefinition);
        }

        if (showConnection) {
            connectionPanel = new ConnectionPanel(connectionDefinition);
            connectionPanel.setLocation(0, column0Top);
            column0Top += connectionPanel.getHeight();
            desktopPane.add(connectionPanel);
        }

        if (showRunScript) {
            runScriptPanel = new RunScriptPanel(connectionPanel.getDeviceHelper(), playerDefinition, shell, deviceDefinition, viewDefinition, scriptDefinition, mapPanel, customHandler);
            runScriptPanel.setLocation(0, column0Top);
            column0Top += runScriptPanel.getHeight();
            desktopPane.add(runScriptPanel);
        }

        if (showMapper) {
            mapperPanel = new MapperPanel(mapPanel);
            mapperPanel.setLocation(0, column0Top);
            column0Top += mapperPanel.getHeight();
            desktopPane.add(mapperPanel);
        }

        setContentPane(desktopPane);


        this.pack();

        setLocationRelativeTo(null);

        setVisible(true);
    }
}
