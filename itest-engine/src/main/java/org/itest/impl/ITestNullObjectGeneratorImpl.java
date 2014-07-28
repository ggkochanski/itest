package org.itest.impl;

import java.lang.reflect.Type;
import java.util.Map;

import org.itest.ITestConfig;
import org.itest.ITestContext;
import org.itest.param.ITestParamState;

public class ITestNullObjectGeneratorImpl extends ITestRandomObjectGeneratorImpl {

    public ITestNullObjectGeneratorImpl(ITestConfig iTestConfig) {
        super(iTestConfig);
    }

    @Override
    public <T> T generateRandom(Class<T> clazz, ITestParamState iTestState, Map<String, Type> itestGenericMap, ITestContext iTestContext) {
        if ( null == iTestState && !clazz.isPrimitive() ) {
            return null;
        }
        return super.generateRandom(clazz, iTestState, itestGenericMap, iTestContext);
    }

}
