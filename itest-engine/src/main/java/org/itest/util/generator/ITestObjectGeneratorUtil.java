package org.itest.util.generator;

import java.util.Collections;

import org.itest.ITestConfig;
import org.itest.ITestConstants;
import org.itest.impl.ITestContextImpl;
import org.itest.impl.ITestParamAssignmentImpl;
import org.itest.param.ITestParamAssignment;
import org.itest.param.ITestParamState;

public class ITestObjectGeneratorUtil {

    public static <T> T generateObject(Class<T> clazz, ITestConfig iTestConfig, ITestObjectDefinition... definitions) {
        T res;
        ITestParamAssignment[] iTestParamAssignments = new ITestParamAssignment[definitions.length];
        for (int i = 0; i < definitions.length; i++) {
            ITestParamState params = iTestConfig.getITestParamLoader().loadITestParam(definitions[i].getClazz(), definitions[i].getUse());
            iTestParamAssignments[i] = new ITestParamAssignmentImpl(definitions[i].getTransform(), params);
        }
        ITestParamState merged = iTestConfig.getITestParamsMerger().merge(iTestParamAssignments).getElement(ITestConstants.THIS);
        res = (T) iTestConfig.getITestObjectGenerator().generate(clazz, merged, Collections.EMPTY_MAP, new ITestContextImpl(Collections.EMPTY_MAP));
        return res;
    }
}
