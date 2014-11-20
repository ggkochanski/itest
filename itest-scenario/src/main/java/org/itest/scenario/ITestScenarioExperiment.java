package org.itest.scenario;

/**
 * Created by rumcajs on 11/13/14.
 */
public class ITestScenarioExperiment<A extends ITestScenarioAction, S extends ITestScenario<A>> {
    String description;

    S scenario;

    ITestScenarioParamsHolder params = new ITestScenarioParamsHolder();

    public ITestScenario getScenario() {
        return scenario;
    }

    public ITestScenarioParamsHolder getParams() {
        return params;
    }
}
