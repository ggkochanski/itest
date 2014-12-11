package org.itest.scenario;

import org.itest.param.ITestParamState;
import org.itest.scenario.util.ITestSuperObjectHolder;

/**
 * Created by rumcajs on 11/13/14.
 */
public class ITestScenarioExperiment<A extends ITestScenarioAction, S extends ITestScenario<A>> {
    String description;

    S scenario;

    private ITestSuperObjectHolder<Object> params = new ITestSuperObjectHolder<Object>();
    private ITestSuperObjectHolder<ITestParamState> verify = new ITestSuperObjectHolder<ITestParamState>();

    public ITestScenario getScenario() {
        return scenario;
    }

    public ITestSuperObjectHolder<Object> getParams() {
        return params;
    }

    public ITestSuperObjectHolder<ITestParamState> getVerifiers() {
        return verify;
    }

    ;
}
