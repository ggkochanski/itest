/**
 * <pre>
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 Grzegorz Kochański
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * </pre>
 */
package org.itest.test;

import org.itest.ITestExecutor;
import org.itest.config.ITestConfigImpl;
import org.itest.executor.ITestExecutorUtil;
import org.itest.impl.ITestDeclarativeObjectGeneratorImpl;
import org.itest.impl.ITestRandomObjectGeneratorImpl;
import org.itest.test.example1.SimpleExample;
import org.itest.test.example2.InterfaceExample;
import org.itest.test.example3.ReuseTestDataExample;
import org.itest.test.example4.CollectionsExample;
import org.itest.test.example5.DataProviderExample;
import org.itest.test.example7.ExternalTestDefinition;
import org.itest.test.example8.ReferenceExample;
import org.itest.test.example9.ValueClassExample;
import org.junit.Assert;
import org.junit.Test;

public class ITestExecutorTest {

    @Test
    public void randomObjectGeneratorTest() {
        ITestConfigImpl iTestConfigImpl = new ITestConfigImpl();
        iTestConfigImpl.setITestObjectGenerator(new ITestRandomObjectGeneratorImpl(iTestConfigImpl));
        ITestExecutor executor = ITestExecutorUtil.buildExecutor(iTestConfigImpl);
        performTests(executor);
    }

    @Test
    public void single() {
        ITestConfigImpl iTestConfigImpl = new ITestConfigImpl();
        iTestConfigImpl.setITestObjectGenerator(new ITestRandomObjectGeneratorImpl(iTestConfigImpl));
        ITestExecutor executor = ITestExecutorUtil.buildExecutor(iTestConfigImpl);
        Assert.assertEquals("", executor.performTestsFor(1, ValueClassExample.class));
    }

    @Test
    public void declarativeObjectGeneratorTest() {
        ITestConfigImpl iTestConfigImpl = new ITestConfigImpl();
        iTestConfigImpl.setITestObjectGenerator(new ITestDeclarativeObjectGeneratorImpl(iTestConfigImpl));
        ITestExecutor executor = ITestExecutorUtil.buildExecutor(iTestConfigImpl);
        performTests(executor);
    }

    void performTests(ITestExecutor executor) {
        Assert.assertEquals("", executor.performTestsFor(27, SimpleExample.class, InterfaceExample.class, ReuseTestDataExample.class, CollectionsExample.class,
                DataProviderExample.class, ExternalTestDefinition.class, ReferenceExample.class, ValueClassExample.class));
    }
}
