package org.itest.impl;

import org.itest.ITestConfig;
import org.itest.ITestConstants;
import org.itest.exception.ITestDeclarationNotFoundException;
import org.itest.exception.ITestException;
import org.itest.impl.util.IoUtils;
import org.itest.param.ITestParamLoader;
import org.itest.param.ITestParamState;

import java.io.IOException;
import java.io.InputStream;

public class ITestParamLoaderImpl implements ITestParamLoader {

    private final ITestConfig iTestConfig;

    public ITestParamLoaderImpl(ITestConfig iTestConfig) {
        this.iTestConfig = iTestConfig;
    }

    @Override
    public ITestParamState loadITestParam(Class<?> iTestClass, String use) {

        String resourceName = resourceName(iTestClass, use);
        boolean namedFileFound = true;
        InputStream is = iTestClass.getClassLoader().getResourceAsStream(resourceName);
        if ( null == is ) {
            resourceName = new StringBuilder(128).append(iTestClass.getName().replace('.', '/')).append(".itest.json").toString();
            is = iTestClass.getClassLoader().getResourceAsStream(resourceName);
            if ( null == is ) {
                throw new ITestDeclarationNotFoundException("File (" + resourceName + ") not found.");
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
            throw new ITestException("Error parsing " + resourceName,e);
        }
        if ( !namedFileFound ) {
            initParams = initParams.getElement(use);
        }
        if ( null == initParams ) {
            throw new ITestDeclarationNotFoundException("Data definition for test (" + use + ") not found in " + resourceName);
        }
        ITestParamStateImpl res = new ITestParamStateImpl();
        res.addElement(ITestConstants.THIS, initParams);
        return res;
    }

    public static String resourceName(Class<?> iTestClass, String name) {
        return new StringBuilder(128).append(iTestClass.getName().replace('.', '/')).append('.').append(name).append(".itest.json").toString();

    }
}
