package org.itest.scenario;

import org.itest.ITestSuperObject;
import org.itest.exception.ITestException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rumi on 2014-11-13.
 */
public class ITestScenarioReflectionEnvironment extends ITestScenarioEnvironment implements ITestSuperObject {
    private Map<String, Object> fields = new HashMap<String, Object>();

    @Override
    public Object execute(String name,ITestScenarioAction action, Object[] args) {
        try {
            Object target = fields.get(action.getName());
            Method m = target.getClass().getMethod(action.getMethodName(), action.getParamTypes());
            Object result = m.invoke(target, args);
            return result;
        } catch (Exception e) {
            throw new ITestException("Error executing action "+name+" (" + action + ")", e);
        }
    }

    @Override
    public void setField(String name, Object value) {
        fields.put(name, value);
    }

}
