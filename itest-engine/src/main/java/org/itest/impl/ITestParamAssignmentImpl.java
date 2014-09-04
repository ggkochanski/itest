package org.itest.impl;

import org.itest.param.ITestParamAssignment;
import org.itest.param.ITestParamState;

public class ITestParamAssignmentImpl implements ITestParamAssignment {

    private final String transformation;

    private final ITestParamState itestParamState;

    public ITestParamAssignmentImpl(String transformation, ITestParamState itestParamState) {
        this.transformation = transformation;
        this.itestParamState = itestParamState;
    }

    public String getTransformation() {
        return transformation;
    }

    public ITestParamState getITestParamState() {
        return itestParamState;
    }

}
