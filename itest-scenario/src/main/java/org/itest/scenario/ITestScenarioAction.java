package org.itest.scenario;

import java.util.Arrays;

/**
 * Created by rumcajs on 11/13/14.
 */
public class ITestScenarioAction {
    String name;

    String methodName;

    Class[] paramTypes;

    public String getName() {
        return name;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class[] getParamTypes() {
        return paramTypes;
    }

    @Override
    public String toString() {
        return "ITestAction{" + "name='" + name + '\'' + ", methodName='" + methodName + '\'' + ", paramTypes=" + Arrays.toString(paramTypes) + '}';
    }
}
