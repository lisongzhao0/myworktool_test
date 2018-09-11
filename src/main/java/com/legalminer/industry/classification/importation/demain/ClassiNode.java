package com.legalminer.industry.classification.importation.demain;

import javax.swing.tree.TreeNode;
import java.util.*;

public class ClassiNode implements TreeNode {

    private ClassiNode parent;
    private List<ClassiNode> childs = new ArrayList<>();
    private Map<String, ClassiNode> level_node = new HashMap<>();
    private String market;
    private String uuid;
    private String level;
    private Object userObject;

    public ClassiNode() {}

    public String getMarket() {
        return market;
    }

    public String getUuid() {
        return uuid;
    }

    public int getDepth() {
        int depth = 0;
        ClassiNode current = getParent();
        while (null!=current) {
            depth ++;
            current = current.getParent();
        }
        return depth;
    }

    public String getLevel() {
        return level;
    }

    public Object getUserObject() {
        return userObject;
    }

    public boolean parentIsRoot() {
        return getParent()!=null && getParent().getParent()==null;
    }

    public ClassiNode setParent(ClassiNode parent) {
        this.parent = parent;
        return this;
    }

    public ClassiNode setMarket(String market) {
        this.market = market;
        return this;
    }

    public ClassiNode setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public ClassiNode setLevel(String level) {
        this.level = level;
        return this;
    }

    public ClassiNode addChild(ClassiNode child) {
        if (this.level_node.containsKey(child.getLevel())) {
            return this.level_node.get(child.getLevel());
        }
        this.level_node.put(child.getLevel(), child);
        this.childs.add(child);
        child.parent = this;
        return this;
    }

    public ClassiNode setUserObject(Object userObject) {
        this.userObject = userObject;
        return this;
    }

    @Override
    public String toString() {
        return null==parent ? "RootNode" : getLevel1234();
    }

    public String getLevel1234() {
        String name = level;

        ClassiNode parent = getParent();
        while (null!=parent) {
            if (null!=parent.getParent()) {
                name = parent.getLevel() + " | " + name;
            }
            parent = parent.getParent();
        }

        return name;
    }

    //=============================================
    @Override
    public ClassiNode getChildAt(int childIndex) {
        if (childIndex<0 && childIndex>=childs.size()) {
            throw new RuntimeException("Not node at index "+childIndex);
        }
        return childs.get(childIndex);
    }

    @Override
    public int getChildCount() {
        return childs.size();
    }

    public ClassiNode getParent() {
        return parent;
    }

    @Override
    public int getIndex(TreeNode node) {
        return childs.indexOf(node);
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    @Override
    public boolean isLeaf() {
        return childs.isEmpty();
    }

    @Override
    public Enumeration<ClassiNode> children() {
        return new Enumeration<ClassiNode>() {
            int count = 0;

            public boolean hasMoreElements() {
                return count < childs.size();
            }

            public ClassiNode nextElement() {
                synchronized (ClassiNode.this) {
                    if (count < childs.size()) {
                        return childs.get(count++);
                    }
                }
                throw new NoSuchElementException("Vector Enumeration");
            }
        };
    }


    public void removeAllChildren() {
        for (int i = getChildCount()-1; i >= 0; i--) {
            ClassiNode child = childs.remove(i);
            child.setParent(null);
        }
    }
}
