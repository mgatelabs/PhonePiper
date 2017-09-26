package com.mgatelabs.ffbe.ui.panels;

import com.google.common.collect.ImmutableList;
import com.mgatelabs.ffbe.ui.utils.CustomHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/25/2017.
 */
public class LogPanel extends JInternalFrame {

    private CustomHandler customHandler;

    private JTable table;

    private DefaultTableModel defaultTableModel;

    private Timer timer;

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public LogPanel(CustomHandler customHandler) {
        super("Log", true, false, true, true);
        this.customHandler = customHandler;

        build();
    }

    private void build() {

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 1.0f;
        c.weighty = 1.0f;

        defaultTableModel = new DefaultTableModel();
        defaultTableModel.addColumn("Class");
        defaultTableModel.addColumn("Time");
        defaultTableModel.addColumn("Level");
        defaultTableModel.addColumn("Msg");

        table = new JTable(defaultTableModel);

        JScrollPane jScrollPane = new JScrollPane(table);

        this.add(jScrollPane, c);

        this.pack();

        this.setVisible(true);

        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ImmutableList<LogRecord> records = customHandler.getEvents();
                if (records.size() > 0) {
                    for (LogRecord logRecord: records) {
                        defaultTableModel.insertRow(0, new String[]{logRecord.getSourceClassName().substring(logRecord.getSourceClassName().lastIndexOf('.')), sdf.format(new Date(logRecord.getMillis())), logRecord.getLevel().getName(), logRecord.getMessage()});
                    }
                }

                while (defaultTableModel.getRowCount() > 50) {
                    defaultTableModel.removeRow(defaultTableModel.getRowCount() - 1);
                }
            }
        });

        timer.start();
    }
}
