package com.mgatelabs.piper.ui.panels;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mgatelabs.piper.shared.details.*;
import com.mgatelabs.piper.ui.panels.utils.CommonNode;
import com.mgatelabs.piper.ui.panels.utils.NodeType;
import com.mgatelabs.piper.ui.panels.utils.TreeUtils;
import com.mgatelabs.piper.ui.utils.Constants;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
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
public class ScriptPanel extends JInternalFrame {

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


    public ScriptPanel(JFrame parent, ViewDefinition viewDefinition, ScriptDefinition scriptDefinition) {
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
            JMenuItem item = new JMenuItem("Edit Value");
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    CommonNode node = getSelectedNode();
                    VarDefinition varDefinition = getSelectedValue(VarDefinition.class);
                    if (node != null && varDefinition != null) {
                        if (varDefinition.getType() == VarType.INT) {
                            String newValue = showChangeIntValuePopup(varDefinition.getName(), varDefinition.getValue());
                            if (newValue.length() > 0) {
                                varDefinition.setValue(newValue);
                                issueNodeRefresh(node);
                            }
                        }
                    }
                }
            });
            varItemMenu.add(item);
        }
        varItemMenu.addSeparator();
        {
            JMenuItem item = new JMenuItem("Delete");
            varItemMenu.add(item);
        }

        /////

        stateMenu = new JPopupMenu("States");
        {
            JMenuItem item = new JMenuItem("New State");
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String stateId = showNewIdPopup("State");
                    if (stateId.length() == 0) return;

                    for (String existingStateId : scriptDefinition.getStates().keySet()) {
                        if (existingStateId.equals(stateId)) {
                            showMessage("Duplicate State", "A State with the Id: " + stateId + " already exists!");
                            return;
                        }
                    }

                    StateDefinition stateDefinition = new StateDefinition();
                    stateDefinition.setId(stateId);
                    stateDefinition.setName(stateId);
                    stateDefinition.setStatements(Lists.newArrayList());

                    scriptDefinition.getStates().put(stateId, stateDefinition);

                    StateNode node = new StateNode(stateDefinition);

                    stateNodes.put(stateId, node);
                    stateNode.add(node);

                    issueNewChild(node, stateNode);
                }
            });
            stateMenu.add(item);
        }

        stateItemMenu = new JPopupMenu("State");
        {
            JMenuItem item = new JMenuItem("New Statement");
            stateItemMenu.add(item);
        }
        {
            JMenuItem item = new JMenuItem("Change Name");
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    StateDefinition stateDefinition = getSelectedValue(StateDefinition.class);
                    if (stateDefinition != null) {
                        String newName = showChangeNamePopup("State", stateDefinition.getName());
                        if (newName.length() > 0) {
                            stateDefinition.setName(newName);
                            DefaultTreeModel model = (DefaultTreeModel) objectTree.getModel();
                            model.nodeChanged(stateNodes.get(stateDefinition.getId()));
                        }
                    }
                }
            });
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

    private void issueNewChild(CommonNode newNode, CommonNode parentNode) {
        DefaultTreeModel model = (DefaultTreeModel) objectTree.getModel();
        int lastIndex = parentNode.getChildCount();
        if (lastIndex > 0) lastIndex--;
        model.insertNodeInto(newNode, parentNode, lastIndex);
    }

    private void issueNodeRefresh(CommonNode node) {
        DefaultTreeModel model = (DefaultTreeModel) objectTree.getModel();
        model.nodeChanged(node);
    }

    private String showNewIdPopup(String type) {
        String value = JOptionPane.showInputDialog(parent, "Id for new " + type + ":");
        if (value != null && Constants.ID_PATTERN.matcher(value).matches()) {
            return value;
        }
        return "";
    }

    private String showChangeNamePopup(String type, String oldValue) {
        String value = JOptionPane.showInputDialog(parent, "Name for " + type + ":", oldValue);
        if (value != null && value.trim().length() > 0) {
            return value.trim();
        }
        return "";
    }

    private String showChangeIntValuePopup(String varName, String oldValue) {
        String value = JOptionPane.showInputDialog(parent, "New Integer value for " + varName + ":", oldValue);
        if (value != null && value.trim().length() > 0) {
            value = value.trim();
            try {
                Integer.parseInt(value);
            } catch (Exception ex) {
                return "";
            }
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

        scriptDetailComponent = new ScriptDetailComponent(parent, viewDefinition, scriptDefinition);

        JSplitPane leftRightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, objectScrollPane, scriptDetailComponent);
        leftRightSplit.setDividerLocation(300);

        this.add(leftRightSplit, c);

        variableNodes = Maps.newHashMap();
        stateNodes = Maps.newHashMap();

        fillItems();

        TreeUtils.expandAllNodes(objectTree, 0, objectTree.getRowCount());

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
                        } else {
                            if (menu == statementItemMenu) {
                                StatementDefinition statementDefinition = getSelectedValue(StatementDefinition.class);
                                if (statementDefinition != null) {
                                    scriptDetailComponent.setupForStatement(statementDefinition);
                                }
                            }
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

    public CommonNode getSelectedNode() {
        TreePath sel = objectTree.getSelectionPath();
        if (sel == null) return null;
        Object lastObject = sel.getLastPathComponent();
        if (lastObject instanceof CommonNode) {
            return (CommonNode) lastObject;
        }
        return null;
    }

    public <T> T getSelectedValue(Class<T> cls) {
        TreePath sel = objectTree.getSelectionPath();
        if (sel == null) return null;
        Object lastObject = sel.getLastPathComponent();
        if (lastObject instanceof CommonNode) {
            CommonNode node = (CommonNode) lastObject;
            if (node.getUserObject() != null && node.getUserObject().getClass().isAssignableFrom(cls)) {
                return (T) node.getUserObject();
            }
        }
        return null;
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
