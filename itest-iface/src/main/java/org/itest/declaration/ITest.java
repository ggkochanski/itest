package org.itest.declaration;

import org.itest.param.ITestParamState;

public interface ITest {

    String name();

    ITestRef[] initRef();

    ITestParamState init();

    ITestParamState verify();
}
