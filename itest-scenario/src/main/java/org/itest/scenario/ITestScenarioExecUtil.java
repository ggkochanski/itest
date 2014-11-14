package org.itest.scenario;

import org.itest.ITestConfig;
import org.itest.ITestConstants;
import org.itest.impl.ITestContextImpl;
import org.itest.param.ITestParamState;
import org.itest.scenario.ITestScenario.Action;

import java.util.Collections;

/**
 * Created by rumcajs on 11/14/14.
 */
public class ITestScenarioExecUtil {

    public static <ENV extends ITestEnvironment, A extends ITestAction, S extends ITestScenario<A>, EXP extends ITestExperiment<A, S>> void executeExperiment(
            Class<EXP> expClass, String expName, ENV env, ITestConfig iTestConfig) {
        EXP experiment = generateITestObject(expClass, expName, iTestConfig);
        for (Object o : experiment.getScenario().getActions()) {
            Action action = (Action) o;
            Object[] args = new Object[action.getParams().length];
            for (int i = 0; i < args.length; i++) {
                args[i] = experiment.getParams().get(action.getParams()[i]);
            }
            Object result = env.execute(action.getITestAction(), args);
            if ( null != action.getResult() ) {
                experiment.getParams().put(action.getResult(), result);
            }
        }
    }

    public static <T> T generateITestObject(Class<T> clazz, String name, ITestConfig iTestConfig) {
        ITestParamState param = iTestConfig.getITestParamLoader().loadITestParam(clazz, name).getElement(ITestConstants.THIS);
        return (T) iTestConfig.getITestObjectGenerator().generate(clazz, param, Collections.EMPTY_MAP, new ITestContextImpl(param, Collections.EMPTY_MAP));
    }
}
