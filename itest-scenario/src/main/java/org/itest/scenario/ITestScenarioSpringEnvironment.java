package org.itest.scenario;

import org.itest.exception.ITestException;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by rumcajs on 11/14/14.
 */
public class ITestScenarioSpringEnvironment extends ITestScenarioEnvironment {
    private final ApplicationContext ctx;

    public ITestScenarioSpringEnvironment(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Object execute(ITestScenarioAction action, Object[] args) {
        try {
            Object bean = ctx.getBean(action.getName());
            Method m = bean.getClass().getMethod(action.getMethodName(), action.getParamTypes());
            return m.invoke(bean, args);
        } catch (Exception e) {
            throw new ITestException("Error executing action (" + action + ") for params (" + Arrays.asList(args) + ")", e);
        }
    }
}
