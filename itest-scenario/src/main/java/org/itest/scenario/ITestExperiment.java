package org.itest.scenario;

import java.util.Map;

/**
 * Created by rumcajs on 11/13/14.
 */
public class ITestExperiment<A extends ITestAction, S extends ITestScenario<A>> {
    String description;

    S scenario;

    Map<String, Object> params;

    public ITestScenario getScenario() {
        return scenario;
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
