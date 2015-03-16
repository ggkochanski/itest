package org.itest.test.issue;

import org.hamcrest.CoreMatchers;
import org.itest.ITestExecutor;
import org.itest.annotation.ITest;
import org.itest.annotation.ITests;
import org.itest.config.ITestConfigImpl;
import org.itest.executor.ITestExecutorUtil;
import org.junit.Assert;

/**
 * Created by rumcajs on 3/16/15.
 */
public class ITestGenericArrayTest {
    static class Test<T> {
        T[] array;
    }

    @ITests(@ITest(init = "A:[{array:[a,b,c]}]", verify = "A:[{array:[a,b,c]}]"))
    public void test(Test<String> t) {
        return;
    }
    @org.junit.Test
    public void genericArrayTest() {
        ITestExecutor executor = ITestExecutorUtil.buildExecutor(new ITestConfigImpl());
        Assert.assertThat(executor.performTestsFor(3, ITestGenericArrayTest.class), CoreMatchers.is(""));
    }

}
