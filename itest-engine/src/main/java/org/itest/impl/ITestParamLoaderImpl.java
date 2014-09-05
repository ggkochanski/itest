package org.itest.impl;

import java.io.IOException;
import java.io.InputStream;

import org.itest.ITestConfig;
import org.itest.ITestConstants;
import org.itest.exception.ITestException;
import org.itest.impl.util.IoUtils;
import org.itest.param.ITestParamLoader;
import org.itest.param.ITestParamState;

public class ITestParamLoaderImpl implements ITestParamLoader {

    private final ITestConfig iTestConfig;

    public ITestParamLoaderImpl(ITestConfig iTestConfig) {
        this.iTestConfig = iTestConfig;
    }

    @Override
    public ITestParamState loadITestParam(Class<?> iTestClass, String use) {
        String resourceName = new StringBuilder(128).append(iTestClass.getName().replace('.', '/')).append('.').append(use).append(".itest").toString();
        boolean namedFileFound = true;
        InputStream is = iTestClass.getClassLoader().getResourceAsStream(resourceName);
        if ( null == is ) {
            resourceName = new StringBuilder(128).append(iTestClass.getName().replace('.', '/')).append(".itest").toString();
            is = iTestClass.getClassLoader().getResourceAsStream(resourceName);
            if ( null == is ) {
                throw new ITestException("File (" + resourceName + ") not found.");
            }
            namedFileFound = false;
        }
        String init;
        byte[] buffer = new byte[1024];
        try {
            init = new String(IoUtils.readBytes(is, buffer));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ITestParamState initParams;
        try {
            initParams = iTestConfig.getITestParamParser().parse(init);
        } catch (RuntimeException e) {
            throw new ITestException("Error parsing " + resourceName);
        }
        if ( !namedFileFound ) {
            initParams = initParams.getElement(use);
        }
        if ( null == initParams ) {
            throw new ITestException("Data definition for test (" + use + ") not found in " + resourceName);
        }
        ITestParamStateImpl res = new ITestParamStateImpl();
        res.addElement(ITestConstants.THIS, initParams);
        return res;
    }
}
