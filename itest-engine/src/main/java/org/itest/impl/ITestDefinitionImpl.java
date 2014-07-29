package org.itest.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import org.itest.definition.ITestDefinition;
import org.itest.param.ITestParamState;

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
public class ITestDefinitionImpl implements ITestDefinition {

    private final Class<?> iTestClass;

    private final Method iTestMethod;

    private final ITestParamState initPrams;

    private final ITestParamState veryficationParams;

    private final Map<String, Type> iTestGenericMap;

    private final String iTestName;

    private final Map<Class<?>, Map<String, String>> iTestStaticAssignments;

    public ITestDefinitionImpl(Class<?> clazz, Method method, String iTestName, ITestParamState initParams, ITestParamState veryficationParams,
            Map<String, Type> flowGenericMap, Map<Class<?>, Map<String, String>> iTestStaticAssignments) {
        this.iTestClass = clazz;
        this.iTestMethod = method;
        this.initPrams = initParams;
        this.veryficationParams = veryficationParams;
        this.iTestGenericMap = flowGenericMap;
        this.iTestName = iTestName;
        this.iTestStaticAssignments = iTestStaticAssignments;
    }

    @Override
    public Class<?> getITestClass() {
        return iTestClass;
    }

    @Override
    public Method getITestMethod() {
        return iTestMethod;
    }

    @Override
    public ITestParamState getInitParams() {
        return initPrams;
    }

    @Override
    public ITestParamState getVeryficationParams() {
        return veryficationParams;
    }

    @Override
    public Map<String, Type> getITestGenericMap() {
        return iTestGenericMap;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append(getClass().getSimpleName()).append('.').append(iTestMethod.getName()).append(" ").append(iTestName);
        return sb.toString();
    }

    @Override
    public Map<Class<?>, Map<String, String>> getITestStaticAssignments() {
        return iTestStaticAssignments;
    }

    @Override
    public String getITestName() {
        return iTestName;
    }
}
