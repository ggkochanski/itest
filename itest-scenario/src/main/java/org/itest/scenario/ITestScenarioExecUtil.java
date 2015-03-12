package org.itest.scenario;

import org.apache.commons.lang.StringUtils;
import org.itest.ITestConfig;
import org.itest.ITestConstants;
import org.itest.exception.ITestException;
import org.itest.impl.ITestContextImpl;
import org.itest.param.ITestParamState;
import org.itest.scenario.ITestScenario.Action;
import org.itest.scenario.util.ITestSuperObjectHolder;
import org.itest.verify.ITestFieldVerificationResult;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;

/**
 * Created by rumcajs on 11/14/14.
 */
public class ITestScenarioExecUtil {

    public static <ENV extends ITestScenarioEnvironment, A extends ITestScenarioAction, S extends ITestScenario<A>, EXP extends ITestScenarioExperiment<A, S>> Collection<ITestFieldVerificationResult> executeExperiment(
            Class<EXP> expClass, String expName, ENV env, ITestConfig iTestConfig) {
        EXP experiment = generateITestObject(expClass, expName, iTestConfig);
        Collection<ITestFieldVerificationResult> res = new ArrayList<ITestFieldVerificationResult>();
        for (Object e : experiment.getScenario().getActions().entrySet()) {
            Entry<String, Action> entry = (Entry<String, Action>) e;
            Action action = (Action) entry.getValue();

            Object[] args = new Object[null==action.getParams()?0:action.getParams().length];
            for (int i = 0; i < args.length; i++) {
                args[i] = parseParamValue(experiment.getParams(), action.getParams()[i]);
            }
            Object result = env.execute(entry.getKey(), action.getITestAction(), args);
            if (null != action.getResult()) {
                experiment.getParams().setField(action.getResult(), result);
            }
            //verify
            ITestParamState verifier = (ITestParamState) experiment.getVerifiers().getField(entry.getKey());
            Collection<ITestFieldVerificationResult> verifyRes = verify(expName+"."+entry.getKey(), result, action.getVerify(), verifier, iTestConfig);
            if (null != verifyRes) {
                res.addAll(verifyRes);
            }
        }
        return res;
    }

    private static Collection<ITestFieldVerificationResult> verify(String name, Object object, ITestParamState scenarioVerify, ITestParamState expVerify, ITestConfig iTestConfig) {
        Collection<ITestFieldVerificationResult> res = null;
        if (null != expVerify) {
            res = iTestConfig.getITestExecutionVerifier().verify("experiment:" + name, object, expVerify);
        } else if (null != scenarioVerify) {
            res = iTestConfig.getITestExecutionVerifier().verify("scenario:" + name, object, scenarioVerify);
        }
        return res;
    }

    public static <T> T generateITestObject(Class<T> clazz, String name, ITestConfig iTestConfig) {
        ITestParamState param = iTestConfig.getITestParamLoader().loadITestParam(clazz, name).getElement(ITestConstants.THIS);
        return (T) iTestConfig.getITestObjectGenerator().generate(clazz, param,  new ITestContextImpl(param, Collections.EMPTY_MAP));
    }

    private static Object parseParamValue(ITestSuperObjectHolder<Object> params, String paramName) {
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
