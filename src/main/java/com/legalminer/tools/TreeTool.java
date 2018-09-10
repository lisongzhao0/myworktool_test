package com.legalminer.tools;

import com.legalminer.industry.classification.importation.demain.ClassiNode;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

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

    public void getProcessLeaf(ClassiNode root, TreeHanlder leafHandler) {
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
                getProcessLeaf(child, leafHandler);
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
    }
}
