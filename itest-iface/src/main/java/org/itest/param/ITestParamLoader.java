package org.itest.param;

public interface ITestParamLoader {
    ITestParamState loadITestParam(Class<?> iTestClass, String use);
}
