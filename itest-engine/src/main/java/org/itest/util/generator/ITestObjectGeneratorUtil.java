package org.itest.util.generator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import org.itest.ITestConfig;
import org.itest.ITestConstants;
import org.itest.exception.ITestException;
import org.itest.impl.ITestContextImpl;
import org.itest.impl.ITestParamAssignmentImpl;
import org.itest.impl.ITestParamStateImpl;
import org.itest.impl.util.IoUtils;
import org.itest.param.ITestParamAssignment;
import org.itest.param.ITestParamState;

public class ITestObjectGeneratorUtil {

    public static <T> T generateObject(Class<T> clazz, ITestConfig iTestConfig, ITestObjectDefinition... definitions) {
        T res;
        ITestParamAssignment[] iTestParamAssignments = new ITestParamAssignment[definitions.length];
        for (int i = 0; i < definitions.length; i++) {
            ITestParamState params = loadParams(definitions[i].getClazz(), definitions[i].getUseName(), iTestConfig);
            iTestParamAssignments[i] = new ITestParamAssignmentImpl(definitions[i].getTransform(), params);
        }
        ITestParamState merged = iTestConfig.getITestParamsMerger().merge(iTestParamAssignments).getElement(ITestConstants.THIS);
        res = (T) iTestConfig.getITestObjectGenerator().generate(clazz, merged, Collections.EMPTY_MAP, new ITestContextImpl(Collections.EMPTY_MAP));
        return res;
    }

    private static ITestParamState loadParams(Class<?> clazz, String use, ITestConfig iTestConfig) {
        String resourceName = new StringBuilder(128).append(clazz.getName().replace('.', '/')).append(".itest").toString();
        InputStream is = clazz.getClassLoader().getResourceAsStream(resourceName);
        if ( null == is ) {
            throw new ITestException("File (" + resourceName + ") not found.");
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
        ITestParamState namedParams = initParams.getElement(use);
        if ( null == namedParams ) {
            throw new ITestException("Data definition for test (" + use + ") not found in " + resourceName);
        }
        ITestParamStateImpl res = new ITestParamStateImpl();
        res.addElement(ITestConstants.THIS, namedParams);
        return res;
    }

}
