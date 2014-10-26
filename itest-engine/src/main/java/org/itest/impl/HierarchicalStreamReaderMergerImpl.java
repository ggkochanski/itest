package org.itest.impl;

import com.thoughtworks.xstream.converters.ErrorWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import org.itest.ITestConstants;

import java.util.*;

/**
 * Created by rumcajs on 10/26/14.
 */
public class HierarchicalStreamReaderMergerImpl implements HierarchicalStreamReader {
    private HierarchicalStreamReaderAssignment[] assignments;
    private Node root;
    private Node current;

    class Node {
        List<Node> children = new ArrayList<Node>();
        String nodeName;
        Map<String, String> attributes = new LinkedHashMap<String, String>();
        String value;
        int visitedChildIndex;
        Node parent;

        @Override
        public String toString() {
            return nodeName + attributes + "(" + value + "):" + children;
        }
    }

    public HierarchicalStreamReaderMergerImpl(HierarchicalStreamReaderAssignment... assignments) {
        List<Node> roots = new ArrayList<Node>();
        for (HierarchicalStreamReaderAssignment assignment : assignments) {
            Node root = new Node();
            roots.add(root);
            buildTree(root, assignment.getHierarchicalStreamReader());
        }
        this.assignments = assignments;
        Node root = new Node();
        for (int i = 0; i < assignments.length; i++) {
            for (String path : assignments[i].getPath()) {
                if (path.length() == 0) {
                    root = roots.get(i);
                } else {
                    StringTokenizer st = new StringTokenizer(path, ITestConstants.ASSIGN);
                    String targetPath = st.nextToken();
                    Node source = findNode(roots.get(i), st.nextToken());
                    assign(root, targetPath, source);
                }
            }

        }
        root = roots.get(0);
        current = root;
        System.out.println(root);
    }

    private void assign(Node node, String targetPath, Node source) {
        StringTokenizer st = new StringTokenizer(targetPath, ITestConstants.ASSIGN_SEPARATOR);
        Node parent;
        int parentIndex;
        while (st.hasMoreTokens()) {
            String element = st.nextToken();
            int index = 0;
            int idx = element.indexOf('[');
            if (idx >= 0) {
                String iString = element.substring(idx + 1, element.indexOf(']'));
                if ("*".equals(iString)) {
                    index = -1;
                } else {
                    index = Integer.parseInt(iString);
                }
                element = element.substring(0, idx);
            }
            if (index >= 0) {
                int i = 0;
                boolean found = false;
                for (Node child : node.children) {
                    if (child.nodeName.equals(element) || 0 == element.length()) {
                        if (i == index) {
                            node = child;
                            found = true;
                            break;
                        } else {
                            i++;
                        }
                    }
                }
                if (!found) {
                    throw new RuntimeException(targetPath + " not found");
                }
            } else {
                node.children.add(new Node());
                node=node.children.get(node.children.size()-1);
                node.nodeName=element;
            }
        }
    }

    private Node findNode(Node node, String path) {
        StringTokenizer st = new StringTokenizer(path, ITestConstants.ASSIGN_SEPARATOR);
        while (st.hasMoreTokens()) {
            String element = st.nextToken();
            int idx = element.indexOf('[');
            int index = 0;
            if (idx >= 0) {
                index = Integer.parseInt(element.substring(idx + 1), element.indexOf(']'));
                element = element.substring(0, idx);
            }
            int i = 0;
            boolean found = false;
            for (Node child : node.children) {
                if (child.nodeName.equals(element) || 0 == element.length()) {
                    if (i == index) {
                        node = child;
                        found = true;
                        break;
                    } else {
                        i++;
                    }
                }
            }
            if (!found) {
                throw new RuntimeException(path + "not found");
            }
        }
        return node;
    }

    private void buildTree(Node root, HierarchicalStreamReader hierarchicalStreamReader) {
        root.nodeName = hierarchicalStreamReader.getNodeName();
        for (int i = 0; i < hierarchicalStreamReader.getAttributeCount(); i++) {
            root.attributes.put(hierarchicalStreamReader.getAttributeName(i), hierarchicalStreamReader.getAttribute(i));
            root.value = hierarchicalStreamReader.getValue();
        }
        while (hierarchicalStreamReader.hasMoreChildren()) {
            hierarchicalStreamReader.moveDown();
            Node node = new Node();
            root.children.add(node);
            node.parent = root;
            buildTree(node, hierarchicalStreamReader);
            hierarchicalStreamReader.moveUp();
        }
    }

    @Override
    public boolean hasMoreChildren() {
        boolean res = (current.visitedChildIndex < current.children.size());
        //boolean res = assignments[0].getHierarchicalStreamReader().hasMoreChildren();
        System.out.println("hasMoreChildren = " + res);
        return res;
    }

    @Override
    public void moveDown() {
        current.visitedChildIndex++;
        current = current.children.get(current.visitedChildIndex - 1);
//        assignments[0].getHierarchicalStreamReader().moveDown();
        System.out.println("moveDown");
    }

    @Override
    public void moveUp() {
        current = current.parent;
//        assignments[0].getHierarchicalStreamReader().moveUp();
        System.out.println("moveUp");
    }

    @Override
    public String getNodeName() {
        //String nodeName = assignments[0].getHierarchicalStreamReader().getNodeName();
        String nodeName = current.nodeName;
        System.out.println("nodeName = " + nodeName);
        return nodeName;
    }

    @Override
    public String getValue() {
        String value = current.value;
        //String value = assignments[0].getHierarchicalStreamReader().getValue();
        System.out.println("value = " + value);
        return value;
    }

    @Override
    public String getAttribute(String name) {
//        String attribute = assignments[0].getHierarchicalStreamReader().getAttribute(name);
        String attribute = current.attributes.get(name);
        System.out.println("attribute(" + name + ") = " + attribute);
        return attribute;
    }

    @Override
    public String getAttribute(int index) {
        //String attribute = assignments[0].getHierarchicalStreamReader().getAttribute(index);

        Map.Entry<String, String> entry = getAttribuiteEntry(current.attributes, index);
        String attribute = entry.getValue();
        System.out.println("attribute(" + index + ") = " + attribute);
        return attribute;
    }

    private Map.Entry<String, String> getAttribuiteEntry(Map<String, String> attributes, int index) {
        Map.Entry<String, String> entry = null;
        Iterator<Map.Entry<String, String>> entries = current.attributes.entrySet().iterator();
        for (int i = 0; i < index; i++) {
            entry = entries.next();
        }
        return entry;
    }

    @Override
    public int getAttributeCount() {
        //int attributeCount = assignments[0].getHierarchicalStreamReader().getAttributeCount();
        int attributeCount = current.attributes.size();
        System.out.println("attributeCount = " + attributeCount);
        return attributeCount;
    }

    @Override
    public String getAttributeName(int index) {
        //String attributteName = assignments[0].getHierarchicalStreamReader().getAttributeName(index);
        Map.Entry<String, String> entry = getAttribuiteEntry(current.attributes, index);
        String attributteName = entry.getKey();
        System.out.println("attributteName(" + index + ") = " + attributteName);
        return attributteName;
    }

    @Override
    public Iterator getAttributeNames() {
        //   Iterator attributeNames = assignments[0].getHierarchicalStreamReader().getAttributeNames();
        Iterator attributeNames = current.attributes.keySet().iterator();
        //attributeNames = assignments[0].getHierarchicalStreamReader().getAttributeNames();
        //System.out.println("attributeNames = " + attributeNames);
        return attributeNames;
    }

    @Override
    public void appendErrors(ErrorWriter errorWriter) {
        assignments[0].getHierarchicalStreamReader().appendErrors(errorWriter);
        System.out.println("appendErrors");
    }

    @Override
    public void close() {
        assignments[0].getHierarchicalStreamReader().close();
        System.out.println("close");

    }

    @Override
    public HierarchicalStreamReader underlyingReader() {
        HierarchicalStreamReader underlyingReader = assignments[0].getHierarchicalStreamReader().underlyingReader();
        System.out.println("underlyingReader = " + underlyingReader);
        return underlyingReader;
    }

}
