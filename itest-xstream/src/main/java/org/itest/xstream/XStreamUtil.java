package org.itest.xstream;

import org.itest.param.ITestParamState;

import com.thoughtworks.xstream.io.HierarchicalStreamReader;

public class XStreamUtil {
    public static ITestParamState toITestParamNode(HierarchicalStreamReader hierarchicalStreamReader) {
        ITestParamNode root = new ITestParamNode();
        buildTree(root, hierarchicalStreamReader);
        return root;
    }

    private static void buildTree(ITestParamState paramState, HierarchicalStreamReader hierarchicalStreamReader) {
        root.setNodeName(hierarchicalStreamReader.getNodeName());
        for (int i = 0; i < hierarchicalStreamReader.getAttributeCount(); i++) {
            root.attributes().put(hierarchicalStreamReader.getAttributeName(i), hierarchicalStreamReader.getAttribute(i));
            root.setValue(hierarchicalStreamReader.getValue());
        }
        while (hierarchicalStreamReader.hasMoreChildren()) {
            hierarchicalStreamReader.moveDown();
            ITestParamNode node = new ITestParamNode();
            root.children().add(node);
            node.setParent(root);
            buildTree(node, hierarchicalStreamReader);
            hierarchicalStreamReader.moveUp();
        }
    }

}
