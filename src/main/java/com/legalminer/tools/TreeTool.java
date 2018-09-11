package com.legalminer.tools;

import com.legalminer.industry.classification.importation.demain.ClassiNode;

import java.util.*;

public class TreeTool {

    public static final TreeTool newOne() { return new TreeTool(); }


    public ClassiNode getNode(ClassiNode root, String treePath) {
        if (null==root || root.getChildCount()==0) {
            return null;
        }
        String[] levels = treePath.split(" \\| ");
        return getNode(root, levels, 0);
    }

    public ClassiNode getNode(ClassiNode root, String[] treePath) {
        if (null==root || root.getChildCount()==0 || treePath==null || treePath.length == 0) {
            return null;
        }
        return getNode(root, treePath, 0);
    }

    private ClassiNode getNode(ClassiNode root, String[] treePath, int levelIndex) {
        if (null==root) { return null; }
        if (levelIndex>=treePath.length) { return null; }

        String level = treePath[levelIndex];
        Enumeration<ClassiNode> childs = root.children();
        while (childs.hasMoreElements()) {
            ClassiNode child = childs.nextElement();
            if (child.getLevel().equals(level)) {
                if (levelIndex+1 < treePath.length) {
                    return getNode(child, treePath, levelIndex + 1);
                } else {
                    return child;
                }
            }
        }
        return null;
    }

    public List<ClassiNode> getAllLeaf(ClassiNode root) {
        final List<ClassiNode> leaves = new ArrayList<>();
        processLeaf(root, new TreeHanlder() {
            @Override public ClassiNode createNode(ClassiNode rootNode, String[] treePath) { return null; }
            @Override public void processNode(ClassiNode node) { return; }
            @Override public void processLeaf(ClassiNode leaf) { leaves.add(leaf); }
        });
        return leaves;
    }

    public ClassiNode createTree(ClassiNode classifRoot, List<String> treeString) {
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
            ClassiNode level1 = getNode(classifRoot, levelPath);
            if (level1==null) {
                level1 = new ClassiNode().setLevel(levels[0]).setParent(classifRoot);
                classifRoot.addChild(level1);
            }

            ClassiNode level2 = null;
            if (level1!=null && levels.length>=2) {
                levelPath += " | " + levels[1];
                level2 = getNode(classifRoot, levelPath);
                if (level2 == null) {
                    level2 = new ClassiNode().setLevel(levels[1]).setParent(level1);
                    level1.addChild(level2);
                }
            }

            ClassiNode level3 = null;
            if (level2!=null && levels.length>=3) {
                levelPath += " | " + levels[2];
                level3 = getNode(classifRoot, levelPath);
                if (level3 == null) {
                    level3 = new ClassiNode().setLevel(levels[2]).setParent(level2);
                    level2.addChild(level3);
                }
            }

            ClassiNode level4 = null;
            if (level3!=null && levels.length>=4) {
                levelPath += " | " + levels[3];
                level4 = getNode(classifRoot, levelPath);
                if (level4 == null) {
                    level4 = new ClassiNode().setLevel(levels[3]).setParent(level3);
                    level3.addChild(level4);
                }
            }
        }

        return classifRoot;
    }

    public void processLeaf(ClassiNode root, TreeHanlder leafHandler) {
        if (root==null) {
            return;
        }

        if (root.isLeaf()) {
            leafHandler.processLeaf(root);
            return;
        }

        Enumeration<ClassiNode> childs = root.children();
        while (childs.hasMoreElements()) {
            ClassiNode child = childs.nextElement();
            if (child.isLeaf()) {
                leafHandler.processLeaf(child);
            }
            else {
                processLeaf(child, leafHandler);
            }
        }
    }

    public void processNode(ClassiNode root, TreeHanlder nodeHandler) {
        if (root==null) {
            return;
        }

        nodeHandler.processNode(root);

        Enumeration<ClassiNode> childs = root.children();
        while (childs.hasMoreElements()) {
            ClassiNode child = childs.nextElement();
            if (child.isLeaf()) {
                nodeHandler.processNode(child);
            }
            else {
                processNode(child, nodeHandler);
            }
        }
    }

    public ClassiNode createTree(List<String[]> treePaths, TreeHanlder hanlder) {
        ClassiNode root = new ClassiNode();

        Set<String> existed = new HashSet<>();
        for (String[] treePath : treePaths) {
            if (existed.contains(Arrays.deepToString(treePath))) {
                continue;
            }
            existed.add(Arrays.deepToString(treePath));

            hanlder.createNode(root, treePath);
        }

        return root;
    }



    public interface TreeHanlder {
        ClassiNode createNode(ClassiNode rootNode, String[] treePath);
        void processLeaf(ClassiNode leaf);
        void processNode(ClassiNode node);
    }
}
