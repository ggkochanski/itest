package org.itest.scenario;

import org.itest.exception.ITestException;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;

/**
 * Created by rumcajs on 11/14/14.
 */
public class ITestSpringEnvironment extends ITestEnvironment {
    private final ApplicationContext ctx;

    public ITestSpringEnvironment(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Object execute(ITestAction action, Object[] args) {
        try {
            Object bean = ctx.getBean(action.getName());
            Method m = bean.getClass().getMethod(action.getMethodName(), action.getParamTypes());
            return m.invoke(bean, args);
        } catch (Exception e) {
            throw new ITestException("Error executing action (" + action + ")", e);
        }
    }
}
