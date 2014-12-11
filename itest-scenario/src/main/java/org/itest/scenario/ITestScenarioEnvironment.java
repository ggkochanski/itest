package org.itest.scenario;

/**
 * Created by rumcajs on 11/13/14.
 */
public abstract class ITestScenarioEnvironment {
    public abstract Object execute(String name,ITestScenarioAction action, Object[] args);
}
