package org.itest.scenario;

import org.apache.commons.lang.StringUtils;
import org.itest.ITestConfig;
import org.itest.ITestConstants;
import org.itest.exception.ITestException;
import org.itest.impl.ITestContextImpl;
import org.itest.param.ITestParamState;
import org.itest.scenario.ITestScenario.Action;

import java.lang.reflect.Field;
import java.util.Collections;

/**
 * Created by rumcajs on 11/14/14.
 */
public class ITestScenarioExecUtil {

    public static <ENV extends ITestScenarioEnvironment, A extends ITestScenarioAction, S extends ITestScenario<A>, EXP extends ITestScenarioExperiment<A, S>> void executeExperiment(
            Class<EXP> expClass, String expName, ENV env, ITestConfig iTestConfig) {
        EXP experiment = generateITestObject(expClass, expName, iTestConfig);
        for (Object o : experiment.getScenario().getActions()) {
            Action action = (Action) o;

            if (null == action.getParams()) {
                throw new ITestException("Params not specified for action:" + action);
            }
            Object[] args = new Object[action.getParams().length];
            for (int i = 0; i < args.length; i++) {
                args[i] = parseParamValue(experiment.getParams(), action.getParams()[i]);
            }
            Object result = env.execute(action.getITestAction(), args);
            if (null != action.getResult()) {
                experiment.getParams().setField(action.getResult(), result);
            }

        }
    }


    public static <T> T generateITestObject(Class<T> clazz, String name, ITestConfig iTestConfig) {
        ITestParamState param = iTestConfig.getITestParamLoader().loadITestParam(clazz, name).getElement(ITestConstants.THIS);
        return (T) iTestConfig.getITestObjectGenerator().generate(clazz, param, Collections.EMPTY_MAP, new ITestContextImpl(param, Collections.EMPTY_MAP));
    }

    private static Object parseParamValue(ITestScenarioParamsHolder params, String paramName) {
        if (null == paramName) {
            return null;
        }
        String[] paramNames = StringUtils.split(paramName, '.');
        Object paramValue = params.getField(paramNames[0]);
        for (int i = 1; i < paramNames.length; i++) {
            paramValue = getObjectProperty(paramValue, paramNames[i]);
        }
        return paramValue;
    }

    private static Object getObjectProperty(Object paramValue, String paramName) {
        if(null==paramValue){
            return null;
        }
        Class<?> clazz = paramValue.getClass();
        Field f = null;
        do {
            try {
                f = clazz.getDeclaredField(paramName);
                f.setAccessible(true);
            } catch (Exception e) {
            }
        } while (null != (clazz = clazz.getSuperclass()));

        try {
            return f.get(paramValue);
        } catch (IllegalAccessException e) {
            throw new ITestException("Error accessing " + f + " for object of " + clazz);
        }
    }

}
