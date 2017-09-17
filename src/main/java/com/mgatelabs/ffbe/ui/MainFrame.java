package com.mgatelabs.ffbe.ui;

import com.mgatelabs.ffbe.shared.details.PlayerDetail;
import com.mgatelabs.ffbe.shared.mapper.MapDefinition;

import javax.swing.*;
import java.awt.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/12/2017.
 */
public class MainFrame extends JFrame {

    final PlayerDetail playerDetail;

    JDesktopPane desktopPane;
    PlayerPanel playerPanel;

    MapPanel mapPanel;
    MapperPanel mapperPanel;


    public MainFrame() throws HeadlessException {
        super("FFBExecute 0.0.1");
        setMinimumSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(1024, 768));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        PlayerDetail tempPlayerDetail = PlayerDetail.read();// new PlayerDetail();
        if (tempPlayerDetail == null) {
            tempPlayerDetail = new PlayerDetail();
        }
        playerDetail = tempPlayerDetail;

        desktopPane = new JDesktopPane();

        playerPanel = new PlayerPanel(playerDetail);
        desktopPane.add(playerPanel);

        mapPanel = new MapPanel();
        desktopPane.add(mapPanel);
        mapPanel.setLocation(playerPanel.getWidth(), 0);

        mapperPanel = new MapperPanel(mapPanel);
        desktopPane.add(mapperPanel);
        mapperPanel.setLocation(0, playerPanel.getHeight());

        MapDefinition mapDefinition = new MapDefinition();
        mapDefinition.addFloor("start");
        mapPanel.setMap(mapDefinition);

        this.pack();

        setContentPane(desktopPane);
    }
}
