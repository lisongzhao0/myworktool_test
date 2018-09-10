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

    public String getLevel() {
        return level;
    }

    public Object getUserObject() {
        return userObject;
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
    public static ClassiNode getNode(ClassiNode root, String level1234) {
        if (null==root || root.getChildCount()==0) {
            return null;
        }
        String[] levels = level1234.split(" \\| ");
        return getNode(root, levels, 0);
    }

    public static ClassiNode getNode(ClassiNode root, String[] levels, int levelIndex) {
        if (null==root) {
            return null;
        }
        if (levelIndex>=levels.length) {
            return null;
        }

        String level = levels[levelIndex];
        Enumeration<ClassiNode> childs = root.children();
        while (childs.hasMoreElements()) {
            ClassiNode child = childs.nextElement();
            if (child.getLevel().equals(level)) {
                if (levelIndex+1 < levels.length) {
                    return getNode(child, levels, levelIndex + 1);
                } else {
                    return child;
                }
            }
        }
        return null;
    }

    public static List<ClassiNode> getAllLeaf(ClassiNode root) {
        if (root==null) {
            return new ArrayList<>();
        }
        List<ClassiNode> allLeaf = new ArrayList<>();

        if (root.getChildCount()==0) {
            allLeaf.add(root);
            return allLeaf;
        }

        Enumeration<ClassiNode> childs = root.children();
        while (childs.hasMoreElements()) {
            ClassiNode child = childs.nextElement();
            if (child.getChildCount()==0) {
                allLeaf.add(child);
            }
            else {
                List<ClassiNode> subLeaf = getAllLeaf(child);
                allLeaf.addAll(subLeaf);
            }
        }

        return allLeaf;
    }

    public static ClassiNode createTree(ClassiNode classifRoot, List<String> treeString) {
        if (null==classifRoot) {
            classifRoot = new ClassiNode();
        }
        Set<String> existed = new HashSet<>();
        for (String level1234 : treeString) {
            if (existed.contains(level1234)) {
                continue;
            }
            existed.add(level1234);
            String[] levels = level1234.split(" \\| ");

            String levelPath = levels[0];
            ClassiNode level1 = ClassiNode.getNode(classifRoot, levelPath);
            if (level1==null) {
                level1 = new ClassiNode().setLevel(levels[0]).setParent(classifRoot);
                classifRoot.addChild(level1);
            }

            levelPath += " | "+levels[1];
            ClassiNode level2 = ClassiNode.getNode(classifRoot, levelPath);
            if (level2==null) {
                level2 = new ClassiNode().setLevel(levels[1]).setParent(level1);
                level1.addChild(level2);
            }

            levelPath += " | "+levels[2];
            ClassiNode level3 = ClassiNode.getNode(classifRoot, levelPath);
            if (level3==null) {
                level3 = new ClassiNode().setLevel(levels[2]).setParent(level2);
                level2.addChild(level3);
            }

            if (levels.length==4) {
                levelPath += " | " + levels[3];
                ClassiNode level4 = ClassiNode.getNode(classifRoot, levelPath);
                if (level4 == null) {
                    level4 = new ClassiNode().setLevel(levels[3]).setParent(level3);
                    level3.addChild(level4);
                }
            }
        }

        return classifRoot;
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
