package com.mgatelabs.ffbe.ui.panels;

import com.google.common.collect.Maps;
import com.mgatelabs.ffbe.shared.details.*;
import com.mgatelabs.ffbe.ui.utils.Constants;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 10/12/2017
 */
public class StatePanel extends JInternalFrame {

    private final ViewDefinition viewDefinition;
    private final ScriptDefinition scriptDefinition;
    private final JFrame parent;

    JTree objectTree;

    CommonNode rootNode;

    CommonNode variableNode;
    Map<String, VariableNode> variableNodes;

    CommonNode stateNode;
    Map<String, StateNode> stateNodes;

    private ScriptDetailComponent scriptDetailComponent;

    JPopupMenu varMenu;
    JPopupMenu varItemMenu;
    JPopupMenu stateMenu;
    JPopupMenu stateItemMenu;
    JPopupMenu statementMenu;
    JPopupMenu statementItemMenu;


    public StatePanel(JFrame parent, ViewDefinition viewDefinition, ScriptDefinition scriptDefinition) {
        super("States", true, false, true, false);
        this.parent = parent;
        this.viewDefinition = viewDefinition;
        this.scriptDefinition = scriptDefinition;

        buildMenus();

        build();
    }

    private void buildMenus() {
        varMenu = new JPopupMenu("Variable");
        {
            JMenuItem item = new JMenuItem("New (Int)");
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
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
                }
            });
            varMenu.add(item);
        }
        varItemMenu = new JPopupMenu("Variable");
        {
            JMenuItem item = new JMenuItem("Change Name");
            varItemMenu.add(item);
        }
        {
            JMenuItem item = new JMenuItem("Edit Value");
            varItemMenu.add(item);
        }
        varItemMenu.addSeparator();
        {
            JMenuItem item = new JMenuItem("Delete");
            varItemMenu.add(item);
        }

        /////

        stateMenu = new JPopupMenu("State");
        {
            JMenuItem item = new JMenuItem("New State");
            stateMenu.add(item);
        }
        stateItemMenu = new JPopupMenu("State");
        {
            JMenuItem item = new JMenuItem("New Statement");
            stateItemMenu.add(item);
        }
        {
            JMenuItem item = new JMenuItem("Change Name");
            stateItemMenu.add(item);
        }
        stateItemMenu.addSeparator();
        {
            JMenuItem item = new JMenuItem("Delete");
            stateItemMenu.add(item);
        }

        statementMenu = new JPopupMenu("Statement");
        {
            JMenuItem item = new JMenuItem("New");
            statementMenu.add(item);
        }

        statementItemMenu = new JPopupMenu("Statement");
        {
            JMenuItem item = new JMenuItem("Edit");
            statementItemMenu.add(item);
        }
        statementItemMenu.addSeparator();
        {
            JMenuItem item = new JMenuItem("Delete");
            statementItemMenu.add(item);
        }

        JMenuBar mainMenu = new JMenuBar();
        setJMenuBar(mainMenu);

        {
            JMenu fileMenu = new JMenu("File");
            fileMenu.setMnemonic('F');
            mainMenu.add(fileMenu);

            {
                JMenuItem item = new JMenuItem("Save");
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (scriptDefinition.validate()) {
                            if (!scriptDefinition.save()) {
                                showMessage("Error", "Could not save Script Definition");
                            }
                        } else {
                            showMessage("Error", "Script validation failed");
                        }
                    }
                });
                fileMenu.add(item);
            }
        }
    }

    private String showNewIdPopup(String type) {
        String value = JOptionPane.showInputDialog(parent, "Id for new " + type + ":");
        if (value != null && Constants.ID_PATTERN.matcher(value).matches()) {
            return value;
        }
        return "";
    }

    private void showMessage(String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.PLAIN_MESSAGE);
    }

    private void build() {
        setMinimumSize(new Dimension(600, 500));
        setPreferredSize(getMinimumSize());

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        rootNode = new CommonBlockNode(NodeType.ROOT, "Script");

        variableNode = new CommonBlockNode(NodeType.VARIABLE, "Variables");
        rootNode.add(variableNode);

        stateNode = new CommonBlockNode(NodeType.STATE, "States");
        rootNode.add(stateNode);

        objectTree = new JTree(rootNode);

        objectTree.expandRow(0);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 5;
        c.weighty = 1.0f;
        c.weightx = 1.0f;
        JScrollPane objectScrollPane = new JScrollPane(objectTree);

        scriptDetailComponent = new ScriptDetailComponent(viewDefinition, scriptDefinition);

        JSplitPane leftRightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, objectScrollPane, scriptDetailComponent);
        leftRightSplit.setDividerLocation(300);

        this.add(leftRightSplit, c);

        variableNodes = Maps.newHashMap();
        stateNodes = Maps.newHashMap();

        fillItems();

        expandAllNodes(objectTree, 0, objectTree.getRowCount());

        this.pack();

        this.setVisible(true);

        objectTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                Object lastObject = e.getPath().getLastPathComponent();
                if (lastObject instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode) lastObject;
                    Object userObject = defaultMutableTreeNode.getUserObject();
                    if (userObject instanceof VarDefinition) {
                        VarDefinition varDefinition = (VarDefinition) userObject;
                        scriptDetailComponent.setupForVariable(varDefinition);
                        return;
                    } else if (userObject instanceof StatementDefinition) {
                        StatementDefinition statementDefinition = (StatementDefinition) userObject;
                        scriptDetailComponent.setupForStatement(statementDefinition);
                        return;
                    } else if (userObject instanceof StateDefinition) {
                        StateDefinition stateDefinition = (StateDefinition) userObject;
                        scriptDetailComponent.setupForState(stateDefinition);
                        return;
                    }
                }
                scriptDetailComponent.setupForNothing();
            }
        });

        objectTree.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TreePath sel = objectTree.getSelectionPath();
                if (sel == null) return;
                Object lastObject = sel.getLastPathComponent();
                if (lastObject instanceof CommonNode) {
                    CommonNode node = (CommonNode) lastObject;
                    JPopupMenu menu = null;
                    System.out.println(node.getType().name());
                    switch (node.getType()) {
                        case ROOT: {

                        }
                        break;
                        case STATE: {
                            menu = stateMenu;
                        }
                        break;
                        case STATE_ITEM: {
                            menu = stateItemMenu;
                        }
                        break;
                        case VARIABLE: {
                            menu = varMenu;
                        }
                        break;
                        case VARIABLE_ITEM: {
                            menu = varItemMenu;
                        }
                        break;
                        case STATEMENT: {
                            menu = statementMenu;
                        }
                        break;
                        case STATEMENT_ITEM: {
                            menu = statementItemMenu;
                        }
                        break;
                    }
                    if (menu != null) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            menu.show(objectTree, e.getX(), e.getY());
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
    }

    private void fillItems() {

        variableNodes.clear();
        variableNode.removeAllChildren();

        for (VarDefinition varDefinition : scriptDefinition.getVars()) {
            VariableNode node = new VariableNode(varDefinition);
            variableNodes.put(varDefinition.getName(), node);
            variableNode.add(node);
        }

        stateNodes.clear();
        stateNode.removeAllChildren();

        for (StateDefinition stateDefinition : scriptDefinition.getStates().values()) {
            StateNode node = new StateNode(stateDefinition);
            stateNodes.put(stateDefinition.getId(), node);
            stateNode.add(node);
            for (StatementDefinition statementDefinition : stateDefinition.getStatements()) {
                StatementNode statementNode = new StatementNode(statementDefinition);
                node.add(statementNode);
            }
        }

    }

    private void expandAllNodes(JTree tree, int startingIndex, int rowCount) {
        for (int i = startingIndex; i < rowCount; ++i) {
            tree.expandRow(i);
        }

        if (tree.getRowCount() != rowCount) {
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }

    public static enum NodeType {
        ROOT,
        VARIABLE,
        VARIABLE_ITEM,
        STATE,
        STATE_ITEM,
        STATEMENT,
        STATEMENT_ITEM
    }

    public static class CommonNode<T> extends DefaultMutableTreeNode {
        private final NodeType type;

        public CommonNode(NodeType type, T value) {
            super(value);
            this.type = type;
        }

        public NodeType getType() {
            return type;
        }

        @Override
        public T getUserObject() {
            return (T) super.getUserObject();
        }
    }

    public static class CommonBlockNode extends CommonNode<Object> {
        public final String title;

        public CommonBlockNode(NodeType type, String title) {
            super(type, null);
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    public static class VariableNode extends CommonNode<VarDefinition> {


        public VariableNode(VarDefinition userObject) {
            super(NodeType.VARIABLE_ITEM, userObject);
        }

        @Override
        public String toString() {
            VarDefinition varDefinition = getUserObject();
            return varDefinition.getType().name() + " " + varDefinition.getName() + " = " + varDefinition.getValue();
        }
    }

    public static class StateNode extends CommonNode<StateDefinition> {

        public StateNode(StateDefinition userObject) {
            super(NodeType.STATE_ITEM, userObject);
        }

        @Override
        public String toString() {
            return getUserObject().getId() + " - " + getUserObject().getName();
        }
    }

    public static class StatementNode extends CommonNode<StatementDefinition> {

        public StatementNode(StatementDefinition userObject) {
            super(NodeType.STATEMENT_ITEM, userObject);
        }

        @Override
        public String toString() {
            return "if (" + getUserObject().getCondition().toString() + ")";
        }
    }
}
