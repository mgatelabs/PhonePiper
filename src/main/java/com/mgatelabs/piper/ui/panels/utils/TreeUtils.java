package com.mgatelabs.piper.ui.panels.utils;

import javax.swing.*;

/**
 *
 * Created by @mgatelabs (Michael Fuller) on 10/16/2017
 */
public class TreeUtils {
    public static void expandAllNodes(final JTree tree, int startingIndex, int rowCount) {
        for (int i = startingIndex; i < rowCount; ++i) {
            tree.expandRow(i);
        }

        if (tree.getRowCount() != rowCount) {
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }
}
