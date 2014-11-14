package org.itest.scenario;

/**
 * Created by rumcajs on 11/13/14.
 */
public abstract class ITestEnvironment {
    public abstract Object execute(ITestAction action, Object[] args);
}
