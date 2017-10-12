package com.mgatelabs.ffbe.ui.frame;

import com.mgatelabs.ffbe.shared.details.*;
import com.mgatelabs.ffbe.shared.mapper.MapDefinition;
import com.mgatelabs.ffbe.shared.util.AdbShell;
import com.mgatelabs.ffbe.ui.FrameChoices;
import com.mgatelabs.ffbe.ui.panels.*;
import com.mgatelabs.ffbe.ui.utils.CustomHandler;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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

    private ComponentListPanel componentListPanel;
    private ScreenListPanel screenListPanel;
    private StatePanel statePanel;

    private LogPanel logPanel;

    private CustomHandler customHandler;

    private boolean returnRequested;

    public MainFrame(FrameChoices choices, ImageIcon icon) throws HeadlessException {
        super("FFBExecute 0.0.2");
        setIconImage(icon.getImage());
        setMinimumSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(1024, 768));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                if (runScriptPanel != null) {
                    runScriptPanel.stop();
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });

        returnRequested = false;

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
        boolean showMapper = false;
        boolean showAdb = false;
        boolean showConnection = false;
        boolean showRunScript = false;
        boolean showScreens = false;
        boolean showComponents = false;
        boolean showStates = false;

        switch (choices.getAction()) {
            case RUN: {
                // Make sure the view is ready
                viewDefinition = ViewDefinition.read(deviceDefinition.getViewId());

                showPlayer = true;
                showMap = true;
                showAdb = true;
                showConnection = true;
                showRunScript = true;

            }
            break;
            case EDIT: {
                switch (choices.getMode()) {
                    case SCRIPT: {
                        showStates = true;
                    }
                    break;
                    case MAP: {
                        showMap = true;
                    }
                    break;
                    case VIEW: {
                        showConnection = false;
                        showScreens = true;
                        showComponents = true;
                    }
                    break;
                    case DEVICE: {

                    }
                    break;
                }
            }
            break;
            case DELETE: {

            }
            break;
            case CREATE: {
                switch (choices.getMode()) {
                    case MAP: {
                        mapDefinition = new MapDefinition();
                        mapDefinition.addFloor("start");
                        showMap = true;
                        showMapper = true;
                        showAdb = true;
                        showConnection = true;
                    }
                    break;
                    case SCRIPT: {

                    } break;
                    case VIEW: {
                        showScreens = true;
                        showComponents = true;
                    } break;
                }
            }
            break;
        }

        desktopPane = new JDesktopPane();

        logPanel = new LogPanel(customHandler);
        desktopPane.add(logPanel);

        try {
            logPanel.setIcon(true);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }

        int column0Top = 0;
        int column1Top = 0;

        int column0Left = 0;

        if (showConnection) {
            connectionPanel = new ConnectionPanel(connectionDefinition);
            connectionPanel.setLocation(0, column0Top);
            column0Top += connectionPanel.getHeight();
            column0Left = 300;
            desktopPane.add(connectionPanel);
        }

        if (showMap) {
            mapPanel = new MapPanel();
            mapPanel.setMap(mapDefinition);
            mapPanel.setLocation(column0Left, column1Top);
            column1Top += mapPanel.getHeight();
            desktopPane.add(mapPanel);
            mapPanel.setMap(mapDefinition);
        }

        if (showScreens) {
            screenListPanel = new ScreenListPanel(viewDefinition, shell, this);
            screenListPanel.setLocation(column0Left, 0);
            desktopPane.add(screenListPanel);
        }

        if (showComponents) {
            componentListPanel = new ComponentListPanel(deviceDefinition ,viewDefinition, shell, this);
            componentListPanel.setLocation(column0Left + screenListPanel.getWidth(), 0);
            desktopPane.add(componentListPanel);
        }

        if (showStates) {
            statePanel = new StatePanel(viewDefinition, scriptDefinition);
            statePanel.setLocation(column0Left, 0);
            desktopPane.add(statePanel);
        }

        if (showPlayer) {
            playerPanel = new PlayerPanel(playerDefinition);
            playerPanel.setLocation(0, column0Top);
            column0Top += playerPanel.getHeight();
            desktopPane.add(playerPanel);
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

        JScrollPane jScrollPane = new JScrollPane(desktopPane);

        setContentPane(jScrollPane);

        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        JMenu returnMenuItem = new JMenu("Return");

        returnMenuItem.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                returnRequested = true;
                setVisible(false);
            }

            @Override
            public void menuDeselected(MenuEvent e) {

            }

            @Override
            public void menuCanceled(MenuEvent e) {

            }
        });

        menuBar.add(returnMenuItem);

        this.pack();

        setLocationRelativeTo(null);

        setVisible(true);
    }

    public boolean isReturnRequested() {
        return returnRequested;
    }
}
