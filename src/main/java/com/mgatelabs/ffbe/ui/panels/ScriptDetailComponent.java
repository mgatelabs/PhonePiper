package com.mgatelabs.ffbe.ui.panels;

import com.mgatelabs.ffbe.shared.details.*;
import com.mgatelabs.ffbe.ui.panels.utils.CommonNode;
import com.mgatelabs.ffbe.ui.panels.utils.NodeType;
import com.mgatelabs.ffbe.ui.panels.utils.TreeUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 10/15/2017.
 */
public class ScriptDetailComponent extends JPanel {

    JTree detailTree;
    DefaultMutableTreeNode detailNode;
    ViewDefinition viewDefinition;
    ScriptDefinition scriptDefinition;

    StatementNode statementNode;

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

        statementNode = new StatementNode(statementDefinition);

        DefaultTreeModel defaultTreeModel = (DefaultTreeModel) this.detailTree.getModel();

        defaultTreeModel.setRoot(statementNode);

        TreeUtils.expandAllNodes(detailTree, 0, detailTree.getRowCount());
    }

    public void setupForNothing() {
        detailNode.removeAllChildren();
    }

    public enum ConditionRelationship {
        ROOT,
        AND,
        OR
    }

    public static class StatementNode extends CommonNode<StatementDefinition> {

        public StatementNode(StatementDefinition value) {
            super(NodeType.STATEMENT_ITEM, value);

            this.add(new ConditionNode(value.getCondition()));
        }

        @Override
        public String toString() {
            return "Statement";
        }
    }

    public static class ConditionNode extends CommonNode<ConditionDefinition> {

        private ConditionNode parentDefinition;

        private ConditionRelationship relationship;

        public ConditionNode(ConditionDefinition value) {
            this(value, null, ConditionRelationship.ROOT);
        }

        public ConditionNode(ConditionDefinition value, ConditionNode parentDefinition, ConditionRelationship relationship) {
            super(NodeType.CONDITION, value);
            this.parentDefinition = parentDefinition;
            this.relationship = relationship;

            for (ConditionDefinition definition : getUserObject().getAnd()) {
                ConditionNode sub = new ConditionNode(definition, this, ConditionRelationship.AND);
                this.add(sub);
            }

            for (ConditionDefinition definition : getUserObject().getOr()) {
                ConditionNode sub = new ConditionNode(definition, this, ConditionRelationship.OR);
                this.add(sub);
            }

        }

        @Override
        public String toString() {

            switch (relationship) {
                case ROOT: {
                    return ConditionDefinition.getConditionString(getUserObject());
                }
                case AND: {
                    return "AND " + ConditionDefinition.getConditionString(getUserObject());
                }
                case OR: {
                    return "OR " + ConditionDefinition.getConditionString(getUserObject());
                }
            }
            return "ERROR";
        }
    }

    public static class ActionNode extends CommonNode<ActionDefinition> {
        public ActionNode(ActionDefinition value) {
            super(NodeType.ACTION, value);
        }

        @Override
        public String toString() {
            return getUserObject().toString();
        }
    }
}
