package com.mgatelabs.ffbe.ui.panels;

import com.mgatelabs.ffbe.shared.details.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 10/15/2017.
 */
public class ScriptDetailComponent extends JPanel {

    JTree detailTree;
    DefaultMutableTreeNode detailNode;
    ViewDefinition viewDefinition;
    ScriptDefinition scriptDefinition;



    public ScriptDetailComponent(ViewDefinition viewDefinition, ScriptDefinition scriptDefinition) {

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 5;
        c.weighty = 1.0f;
        c.weightx = 1.0f;


        detailNode = new DefaultMutableTreeNode("Detail");

        detailTree = new JTree(detailNode);
        JScrollPane detailScrollPane = new JScrollPane(detailTree);
        this.viewDefinition = viewDefinition;
        this.scriptDefinition = scriptDefinition;
        this.add(detailScrollPane, c);
    }

    public void setupForVariable(VarDefinition varDefinition) {
        detailNode.removeAllChildren();
    }

    public void setupForNewVariable() {

    }

    public void setupForState(StateDefinition stateDefinition) {

    }

    public void setupForStatement(StatementDefinition statementDefinition) {

    }

    public void setupForNothing() {
        detailNode.removeAllChildren();
    }
}
