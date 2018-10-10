package com.mgatelabs.piper.shared;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Credits to <a href="https://stackoverflow.com/a/17490124"/>
 * @author Sanadis
 * Creation Date: 9/27/2018
 */
@JsonPropertyOrder({"id", "description", "level", "root", "leaf", "children"})
public class TreeNode<T> implements Iterable<TreeNode<T>> {

    private String id;
    private String description;
    private T data;
    private final TreeNode<T> parent;
    private final List<TreeNode<T>> children;
    private final List<TreeNode<T>> elementsIndex;

    public TreeNode(String id, T data) {
        this(id, data, null);
    }

    public TreeNode(String id, T data, TreeNode<T> parent) {
        this.id = id;
        this.data = data;
        this.parent = parent;
        this.children = new LinkedList<TreeNode<T>>();
        this.elementsIndex = new LinkedList<TreeNode<T>>();

        if (parent != null)
            parent.addChildNode(this);

        registerChildForSearch(this);
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        if (description == null)
            return getId();
        return description;
    }

    public TreeNode<T> setDescription(String description) {
        this.description = description;
        return this;
    }

    public TreeNode<T> setId(String identifier) {
        this.id = identifier;
        return this;
    }

    public TreeNode<T> setId() {
        if (data != null) {
            this.id = data.toString();
        }
        return this;
    }

    public int getLevel() {
        if (this.isRoot())
            return 0;
        else
            return parent.getLevel() + 1;
    }

    public boolean isLeaf() {
        return children.size() == 0;
    }

    public boolean isRoot() {
        return parent == null;
    }

    @JsonIgnore
    public T getData() {
        return data;
    }

    public TreeNode<T> setData(T data) {
        this.data = data;
        return this;
    }

    public TreeNode<T> findRoot() {
      TreeNode<T> parent = getParent();
      while (parent != null && !parent.isRoot()) {
        parent = parent.getParent();
      }
      return parent;
    }

    @JsonIgnore
    public TreeNode<T> getParent() {
        return parent;
    }

    public TreeNode<T> addChildNode(TreeNode<T> childNode) {
        children.add(childNode);
        return childNode;
    }

    public TreeNode<T> addChildNodes(Collection<TreeNode<T>> children) {
        for (TreeNode<T> child : children) {
            addChildNode(child);
        }
        return this;
    }

    public List<TreeNode<T>> getChildren() {
        return children;
    }

    private void registerChildForSearch(TreeNode<T> node) {
        elementsIndex.add(node);
        if (parent != null)
            parent.registerChildForSearch(node);
    }

    public TreeNode<T> findTreeNode(Comparable<T> cmp) {
        for (TreeNode<T> element : this.elementsIndex) {
            T elData = element.data;
            if (cmp.compareTo(elData) == 0)
                return element;
        }

        return null;
    }

    private int determineValueSiblingIndex() {
        int i = 0;
        for (TreeNode<T> child : parent.getChildren()) {
            if (this == child) {
                return i;
            }
            i++;
        }
        throw new IllegalStateException("This node could not be found amoung the children.");
    }

    @JsonIgnore
    public TreeNode<T> getNextSibling() {
      if (parent == null) {
        return null;
      }

      int index = determineValueSiblingIndex();
      int nextIndex = index + 1;
      if (nextIndex >= 0 && nextIndex < parent.getChildren().size()) {
        return parent.getChildren().get(nextIndex);
      }

      return null;
    }

    @Override
    public String toString() {
        return data != null ? data.toString() : "[data null]";
    }

    public String printNodes() {
        StringBuilder result = new StringBuilder();
        if (!isRoot()) {
          result.append(StringUtils.repeat("| ", getLevel()));

          if (getChildren().size() > 1 || getParent().getChildren().size() > 1) {
            result.append("+- ");
            } else {
                result.append("\\- ");
            }
        }

        result.append(toString())
                .append(" (").append(getLevel()).append(")")
                .append("\r\n");

        for (TreeNode<T> node : children) {
            result.append(node.printNodes());
        }
        return result.toString();
    }

    @Override
    public Iterator<TreeNode<T>> iterator() {
        return new TreeNodeIter<T>(this);
    }

    private static class TreeNodeIter<T> implements Iterator<TreeNode<T>> {

        private enum ProcessStages {
            ProcessParent, ProcessChildCurNode, ProcessChildSubNode
        }

        private TreeNode<T> treeNode;

        private TreeNodeIter(TreeNode<T> treeNode) {
            this.treeNode = treeNode;
            this.doNext = ProcessStages.ProcessParent;
            this.childrenCurNodeIter = treeNode.children.iterator();
        }

        private ProcessStages doNext;
        private TreeNode<T> next;
        private Iterator<TreeNode<T>> childrenCurNodeIter;
        private Iterator<TreeNode<T>> childrenSubNodeIter;

        @Override
        public boolean hasNext() {
            if (this.doNext == ProcessStages.ProcessParent) {
                this.next = this.treeNode;
                this.doNext = ProcessStages.ProcessChildCurNode;
                return true;
            }

            if (this.doNext == ProcessStages.ProcessChildCurNode) {
                if (childrenCurNodeIter.hasNext()) {
                    TreeNode<T> childDirect = childrenCurNodeIter.next();
                    childrenSubNodeIter = childDirect.iterator();
                    this.doNext = ProcessStages.ProcessChildSubNode;
                    return hasNext();
                }

                else {
                    this.doNext = null;
                    return false;
                }
            }

            if (this.doNext == ProcessStages.ProcessChildSubNode) {
                if (childrenSubNodeIter.hasNext()) {
                    this.next = childrenSubNodeIter.next();
                    return true;
                }
                else {
                    this.next = null;
                    this.doNext = ProcessStages.ProcessChildCurNode;
                    return hasNext();
                }
            }

            return false;
        }

        @Override
        public TreeNode<T> next() {
            return this.next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}