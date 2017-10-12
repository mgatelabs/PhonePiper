package com.mgatelabs.ffbe.ui.panels;

import com.google.common.collect.Maps;
import com.mgatelabs.ffbe.shared.details.ScriptDefinition;
import com.mgatelabs.ffbe.shared.details.StateDefinition;
import com.mgatelabs.ffbe.shared.details.StatementDefinition;
import com.mgatelabs.ffbe.shared.details.ViewDefinition;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.Map;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 10/12/2017
 */
public class StatePanel extends JInternalFrame {

    private final ViewDefinition viewDefinition;
    private final ScriptDefinition scriptDefinition;

    JTree stateTree;

    DefaultMutableTreeNode rootNode;

    Map<String, TreeNode> stateNodes;

    public StatePanel(ViewDefinition viewDefinition, ScriptDefinition scriptDefinition) {
        super("States", true, false, true, false);
        this.viewDefinition = viewDefinition;
        this.scriptDefinition = scriptDefinition;

        build();
    }

    private void build() {
        setMinimumSize(new Dimension(300, 500));
        setPreferredSize(getMinimumSize());

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        rootNode = new DefaultMutableTreeNode("States");

        stateTree = new JTree(rootNode);

        stateTree.expandRow(0);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 5;
        c.weighty = 1.0f;
        c.weightx = 1.0f;
        JScrollPane scrollPane = new JScrollPane(stateTree);
        this.add(scrollPane, c);

        stateNodes = Maps.newHashMap();

        for (StateDefinition stateDefinition : scriptDefinition.getStates().values()) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(stateDefinition);
            stateNodes.put(stateDefinition.getId(), node);
            rootNode.add(node);
            for (StatementDefinition statementDefinition : stateDefinition.getStatements()) {
                StatementNode statementNode = new StatementNode(statementDefinition);
                node.add(statementNode);
            }
        }

        this.pack();

        this.setVisible(true);
    }

    public static class StatementNode extends DefaultMutableTreeNode {

        DefaultMutableTreeNode conditionNode;
        DefaultMutableTreeNode actionNode;


        public StatementNode(StatementDefinition userObject) {
            super(userObject);

            conditionNode = new DefaultMutableTreeNode("Condition");
            add(conditionNode);

            actionNode = new DefaultMutableTreeNode("Actions");
            add(actionNode);
        }

        @Override
        public String toString() {
            return "if (" + getUserObject().getCondition().toString() + ")";
        }

        @Override
        public StatementDefinition getUserObject() {
            return (StatementDefinition) super.getUserObject();
        }
    }
}
