package com.mgatelabs.piper.ui.frame;

import com.mgatelabs.piper.Runner;
import com.mgatelabs.piper.shared.details.*;
import com.mgatelabs.piper.shared.mapper.MapDefinition;
import com.mgatelabs.piper.shared.util.AdbShell;
import com.mgatelabs.piper.shared.util.Loggers;
import com.mgatelabs.piper.ui.FrameChoices;
import com.mgatelabs.piper.ui.panels.*;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

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
    private ScriptPanel scriptPanel;

    private LogPanel logPanel;

    private JSplitPane logSplit;

    private boolean returnRequested;

    public MainFrame(FrameChoices choices, ImageIcon icon) throws HeadlessException {
        super();
        setTitle("PhonePiper - " + Runner.VERSION);
        setIconImage(icon.getImage());
        setMinimumSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(1024, 768));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel holder = new JPanel();
        holder.setLayout(new BorderLayout());

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

        playerDefinition = choices.getPlayerDefinition();
        scriptDefinition = choices.getScriptDefinition();
        mapDefinition = choices.getMapDefinition();
        deviceDefinition = choices.getDeviceDefinition();
        viewDefinition = choices.getViewDefinition();

        shell = new AdbShell(deviceDefinition);

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
                if (viewDefinition == null)
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
                        showConnection = true;
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

                    }
                    break;
                    case VIEW: {
                        showScreens = true;
                        showComponents = true;
                    }
                    break;
                }
            }
            break;
        }

        desktopPane = new JDesktopPane();

        logPanel = new LogPanel(Loggers.webLogger);

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
            screenListPanel = new ScreenListPanel(connectionPanel.getDeviceHelper(), deviceDefinition, viewDefinition, shell, this);
            screenListPanel.setLocation(column0Left, 0);
            desktopPane.add(screenListPanel);
        }

        if (showComponents) {
            componentListPanel = new ComponentListPanel(connectionPanel.getDeviceHelper(), deviceDefinition, viewDefinition, shell, this);
            componentListPanel.setLocation(column0Left + screenListPanel.getWidth(), 0);
            desktopPane.add(componentListPanel);
        }

        if (showStates) {
            scriptPanel = new ScriptPanel(this, viewDefinition, scriptDefinition);
            scriptPanel.setLocation(column0Left, 0);
            desktopPane.add(scriptPanel);
        }

        if (showPlayer) {
            playerPanel = new PlayerPanel(playerDefinition);
            playerPanel.setLocation(0, column0Top);
            column0Top += playerPanel.getHeight();
            desktopPane.add(playerPanel);
        }

        if (showRunScript) {
            runScriptPanel = new RunScriptPanel(connectionPanel.getDeviceHelper(), connectionDefinition, playerDefinition, shell, deviceDefinition, viewDefinition, scriptDefinition, mapPanel, Loggers.webLogger);
            //runScriptPanel.setLocation(0, column0Top);
            //column0Top += runScriptPanel.getHeight();

            holder.add(runScriptPanel, BorderLayout.PAGE_START);
            //desktopPane.add(runScriptPanel);
        }

        if (showMapper) {
            mapperPanel = new MapperPanel(mapPanel);
            mapperPanel.setLocation(0, column0Top);
            column0Top += mapperPanel.getHeight();
            desktopPane.add(mapperPanel);
        }

        JScrollPane jScrollPane = new JScrollPane(desktopPane);

        logSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, jScrollPane, logPanel);
        logSplit.setOneTouchExpandable(true);

        holder.add(logSplit, BorderLayout.CENTER);

        setContentPane(holder);

        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        JMenu returnMenuItem = new JMenu("Return");

        returnMenuItem.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                returnRequested = true;
                if (runScriptPanel != null) {
                    runScriptPanel.stop();
                }
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

        logSplit.setDividerLocation(.75);

        setLocationRelativeTo(null);

        setVisible(true);
    }

    public boolean isReturnRequested() {
        return returnRequested;
    }
}
