package com.mgatelabs.ffbe.ui.panels;

import com.mgatelabs.ffbe.shared.details.*;
import com.mgatelabs.ffbe.ui.panels.utils.CommonNode;
import com.mgatelabs.ffbe.ui.panels.utils.NodeType;
import com.mgatelabs.ffbe.ui.panels.utils.TreeUtils;
import com.mgatelabs.ffbe.ui.utils.Constants;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by @mgatelabs (Michael Fuller) on 10/15/2017.
 */
public class ScriptDetailComponent extends JPanel {

    final JTree detailTree;
    final DefaultMutableTreeNode detailNode;
    ViewDefinition viewDefinition;
    ScriptDefinition scriptDefinition;
    private final JFrame parent;

    StatementNode statementNode;
    ActionNode actionNode;

    JPopupMenu actionMenu;

    CommonNode lastNode;

    public ScriptDetailComponent(JFrame parent, ViewDefinition viewDefinition, ScriptDefinition scriptDefinition) {
        this.parent = parent;
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 5;
        c.weighty = 1.0f;
        c.weightx = 1.0f;

        lastNode = null;

        detailNode = new DefaultMutableTreeNode("Detail");

        detailTree = new JTree(detailNode);
        JScrollPane detailScrollPane = new JScrollPane(detailTree);
        this.viewDefinition = viewDefinition;
        this.scriptDefinition = scriptDefinition;
        this.add(detailScrollPane, c);

        detailTree.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TreePath sel = detailTree.getSelectionPath();
                if (sel == null) return;
                Object lastObject = sel.getLastPathComponent();
                lastNode = null;
                if (lastObject instanceof CommonNode) {
                    CommonNode node = (CommonNode) lastObject;
                    lastNode = node;
                    JPopupMenu menu = null;
                    System.out.println(node.getType().name());
                    switch (node.getType()) {

                        case ACTION: {
                            menu = actionMenu;
                        }
                        break;
                        case ACTION_ITEM: {

                        }
                        break;
                        case ACTION_VALUE: {
                            //menu = stateMenu;
                        }
                        break;
                        case ACTION_VAR_NAME: {
                            //menu = stateItemMenu;
                        }
                        break;
                    }
                    if (menu != null) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            menu.show(detailTree, e.getX(), e.getY());
                        } else {


                            //if (menu == statementItemMenu) {
                            //    StatementDefinition statementDefinition = getSelectedValue(StatementDefinition.class);
                            //    if (statementDefinition != null) {
                            //        scriptDetailComponent.setupForStatement(statementDefinition);
                            //    }
                            //}
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        actionMenu = new JPopupMenu("Actions");
        {
            JMenuItem item = new JMenuItem("New Action");
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    ActionType type = showAvailableActionTypes();

                    if (type != null) {

                    }
                    /*
                    String varName = showNewIdPopup("Integer");
                    if (varName.length() > 0) {

                        for (VarDefinition varDefinition : scriptDefinition.getVars()) {
                            if (varDefinition.getName().equals(varName)) {
                                showMessage("Duplicate Variable", "A variable with the name: " + varName + " already exists!");
                                return;
                            }
                        }

                        VarDefinition varDefinition = new VarDefinition();
                        varDefinition.setName(varName);
                        varDefinition.setType(VarType.INT);
                        varDefinition.setValue("0");

                        scriptDefinition.getVars().add(varDefinition);

                        VariableNode node = new VariableNode(varDefinition);
                        variableNode.add(node);
                        variableNodes.put(varDefinition.getName(), node);

                    }
                    scriptDetailComponent.setupForNewVariable();

                    */
                }
            });
            actionMenu.add(item);
        }
    }

    public void setupForVariable(VarDefinition varDefinition) {
        detailNode.removeAllChildren();

        lastNode = null;

        refreshTree();
    }

    private void refreshTree() {
        DefaultTreeModel defaultTreeModel = (DefaultTreeModel) this.detailTree.getModel();
        defaultTreeModel.reload();
        TreeUtils.expandAllNodes(detailTree, 0, detailTree.getRowCount());
    }

    public void setupForNewVariable() {
        detailNode.removeAllChildren();

        lastNode = null;

        refreshTree();
    }

    public void setupForState(StateDefinition stateDefinition) {
        detailNode.removeAllChildren();

        lastNode = null;

        refreshTree();
    }

    public void setupForStatement(StatementDefinition statementDefinition) {

        lastNode = null;

        statementNode = new StatementNode(statementDefinition);
        actionNode = new ActionNode(statementDefinition);

        detailNode.removeAllChildren();

        detailNode.add(statementNode);
        detailNode.add(actionNode);

        refreshTree();
    }

    public void setupForNothing() {
        detailNode.removeAllChildren();

        lastNode = null;

        refreshTree();
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

    public static class ActionNode extends CommonNode<StatementDefinition> {
        public ActionNode(StatementDefinition value) {
            super(NodeType.ACTION, value);
            for (ActionDefinition actionDefinition : value.getActions()) {
                this.add(new ActionDefinitionNode(actionDefinition));
            }
        }

        @Override
        public String toString() {
            return "Actions";
        }
    }

    public static class ActionDefinitionNode extends CommonNode<ActionDefinition> {


        public ActionDefinitionNode(ActionDefinition value) {
            super(NodeType.ACTION_ITEM, value);

            refresh();
        }

        public void refresh() {
            this.removeAllChildren();

            switch (getUserObject().getType().getValueType()) {
                case COMPONENT_ID:
                case STATE_ID:
                case INT:
                case ID:
                case STRING:
                case START_STOP:
                case SCREEN_ID:
                    this.add(new ActionDefinitionValueNode(getUserObject()));
                    break;
                case NONE:

                    break;
            }

            if (getUserObject().getType().isVarNameRequired()) {
                this.add(new ActionDefinitionVarNameNode(getUserObject()));
            }

        }

        @Override
        public String toString() {
            return getUserObject().toString();
        }
    }

    public static class ActionDefinitionValueNode extends CommonNode<ActionDefinition> {

        public ActionDefinitionValueNode(ActionDefinition value) {
            super(NodeType.ACTION_VALUE, value);
        }

        @Override
        public String toString() {
            return "Value: " + getUserObject().getValue();
        }
    }

    public static class ActionDefinitionVarNameNode extends CommonNode<ActionDefinition> {

        public ActionDefinitionVarNameNode(ActionDefinition value) {
            super(NodeType.ACTION_VAR_NAME, value);
        }

        @Override
        public String toString() {
            return "Var: " + getUserObject().getVar();
        }
    }

    // Popups

    private ActionType showAvailableActionTypes() {
        return (ActionType) JOptionPane.showInputDialog(parent, "What type of Action?", "Action Type?", JOptionPane.QUESTION_MESSAGE, null, ActionType.values(), ActionType.TAP);
    }

    private String showNewIdPopup(String type) {
        String value = JOptionPane.showInputDialog(parent, "Id for new " + type + ":");
        if (value != null && Constants.ID_PATTERN.matcher(value).matches()) {
            return value;
        }
        return "";
    }
}
