package org.itest.impl;

import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * Created by rumcajs on 10/26/14.
 */
public class HierarchicalStreamReaderAssignment {
    private final String[] path;
    private final HierarchicalStreamReader extendedHierarchicalStreamReader;

    public HierarchicalStreamReaderAssignment(String[] path, HierarchicalStreamReader extendedHierarchicalStreamReader){
        this.path = path;
        this.extendedHierarchicalStreamReader = extendedHierarchicalStreamReader;
    }

    public String[] getPath() {
        return path;
    }

    public HierarchicalStreamReader getHierarchicalStreamReader() {
        return extendedHierarchicalStreamReader;
    }
}
