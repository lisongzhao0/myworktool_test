package com.legalminer.industry.classification.importation;

import com.legalminer.industry.classification.importation.demain.ClassiNode;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ClassiNodeTest {

    @Test
    public void createTree() {
        List<String> treePath = new ArrayList<>();
        treePath.add("非日常生活消费品 | 零售业 | 多元化零售 | 百货商店");
        treePath.add("非日常生活消费品 | 消费者服务 | 综合消费者服务 | 教育服务");
        treePath.add("非日常生活消费品 | 媒体 | 媒体 | 有线和卫星电视");
        treePath.add("信息技术 | 软件与服务 | 互联网软件与服务 | 互联网软件与服务");
        treePath.add("工业 | 资本品 | 机械制造 | 建筑机械与重型卡车");
        treePath.add("医疗保健 | 医疗保健设备与服务 | 医疗保健设备与用品 | 医疗保健用品");

        ClassiNode rootNode = ClassiNode.createTree(null, treePath);
        List<ClassiNode> allLeaf = ClassiNode.getAllLeaf(rootNode);

        Assert.assertEquals(treePath.size(), allLeaf.size());
        for (ClassiNode leaf : allLeaf) {
            Assert.assertTrue(treePath.contains(leaf.getLevel1234()));
        }
        for (String tp : treePath) {
            Assert.assertNotNull(ClassiNode.getNode(rootNode, tp));
        }
    }
}
