package org.itest.impl;

import org.itest.param.ITestParamAssignment;
import org.itest.param.ITestParamState;

public class ITestParamAssignmentImpl implements ITestParamAssignment {

    private final String[] transformation;

    private final ITestParamState itestParamState;

    public ITestParamAssignmentImpl(String transformation, ITestParamState itestParamState) {
        this(new String[] { transformation }, itestParamState);
    }

    public ITestParamAssignmentImpl(String[] transformation, ITestParamState itestParamState) {
        this.transformation = transformation;
        this.itestParamState = itestParamState;
    }

    @Override
    public String[] getTransformation() {
        return transformation;
    }

    @Override
    public ITestParamState getITestParamState() {
        return itestParamState;
    }

}
