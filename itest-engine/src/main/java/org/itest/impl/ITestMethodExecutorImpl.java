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
package org.itest.impl;

import org.itest.ITestConfig;
import org.itest.ITestConstants;
import org.itest.ITestContext;
import org.itest.definition.ITestDefinition;
import org.itest.exception.ITestException;
import org.itest.exception.ITestMethodExecutionException;
import org.itest.execution.ITestMethodExecutionResult;
import org.itest.execution.ITestMethodExecutor;
import org.itest.param.ITestParamState;
import org.itest.verify.ITestFieldVerificationResult;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

public class ITestMethodExecutorImpl implements ITestMethodExecutor {
    private final ITestConfig iTestConfig;

    public ITestMethodExecutorImpl(ITestConfig iTestConfig) {
        this.iTestConfig = iTestConfig;
    }

    @Override
    public ITestMethodExecutionResult execute(ITestDefinition itestPathDefinition) throws InvocationTargetException {
        Class<?> clazz = itestPathDefinition.getITestClass();
        Method method = itestPathDefinition.getITestMethod();

        ITestParamState paramState = itestPathDefinition.getInitParams();
        paramState=addElementIfMissing(paramState, ITestConstants.THIS, ITestRandomObjectGeneratorImpl.EMPTY_STATE);

        //Map<String, Type> itestGenericMap = itestPathDefinition.getITestGenericMap();
        Map<Class<?>, Map<String, String>> staticAssignments = itestPathDefinition.getITestStaticAssignments();
        ITestContext iTestContext = new ITestContextImpl(paramState,itestPathDefinition.getITestStaticAssignments());
        ITestMethodExecutionResult itestData = new ITestMethodExecutionResult();
        iTestContext.enter(itestData, ITestConstants.THIS);
        Object itestObject = iTestConfig.getITestObjectGenerator().generate(clazz, paramState.getElement(ITestConstants.THIS), iTestContext);
        iTestContext.leave(itestObject);
        itestData.T = itestObject;

        Type parameterTypes[] = method.getGenericParameterTypes();
        Object parameters[] = new Object[parameterTypes.length];
        itestData.A = parameters;
        iTestContext.enter(itestData, ITestConstants.ARG);
        for (int i = 0; i < parameterTypes.length; i++) {
            ITestParamState argState = paramState.getElement(ITestConstants.ARG);
            try {
                iTestContext.enter(parameters, String.valueOf(i));
                parameters[i] = iTestConfig.getITestObjectGenerator().generate(parameterTypes[i],
                        argState == null ? null : argState.getElement(String.valueOf(i)),  iTestContext);
                iTestContext.leave(parameters[i]);
            } catch (ITestException e) {
                e.addPrefix(method + " arg[" + i + "]: ");
                throw e;
            }
        }
        iTestContext.leave(itestData.A);
        // performAssignments(new ITestData(itestObject, parameters), iTestContext.getAssignments());

        try {
            method.setAccessible(true);
            Object res = method.invoke(itestObject, parameters);
            itestData.R = res;
            return itestData;
        } catch (InvocationTargetException e) {
            throw e;
        } catch (Exception e) {
            throw new ITestMethodExecutionException("Error invoking method:" + method, e);
        }
    }

    private ITestParamState addElementIfMissing(ITestParamState paramState, String elementName, ITestParamState emptyState) {
        if(null==paramState.getElement(elementName)){
            ITestParamStateImpl newParamState=new ITestParamStateImpl(paramState);
            newParamState.addElement(elementName,emptyState);
            paramState=newParamState;
        }
        return paramState;
    }

    private void performAssignments(ITestMethodExecutionResult iTestData, Map<List<String>, List<String>> assignments) {
        Map<List<String>, Collection<List<String>>> dependencyMap = new HashMap<List<String>, Collection<List<String>>>();
        for (Map.Entry<List<String>, List<String>> entry : assignments.entrySet()) {
            Collection<List<String>> col = dependencyMap.get(entry.getValue());
            if ( null == col ) {
                col = new ArrayList<List<String>>();
                dependencyMap.put(entry.getValue(), col);
            }
            col.add(entry.getKey());
        }
        for (Map.Entry<List<String>, Collection<List<String>>> entry : dependencyMap.entrySet()) {
            System.out.println(entry.getKey() + " --- " + entry.getValue());
        }
    }

    private void performAssignment(ITestMethodExecutionResult iTestData, List<String> target, List<String> source, Map<List<String>, List<String>> assignments,
            Set<List<String>> visited) {

        // TODO Auto-generated method stub

    }

    static class ITestInvocationTargetExecutionFailure implements ITestFieldVerificationResult {

        private final String message;

        private final InvocationTargetException exception;

        public ITestInvocationTargetExecutionFailure(String msg, InvocationTargetException e) {
            this.message = msg;
            this.exception = e;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String toString() {
            return "Failure " + message + ". Target Invocation exception:" + toString(exception.getTargetException());
        }

        private static String toString(Throwable exception) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            exception.printStackTrace(ps);
            return new String(baos.toByteArray());
        }
    }
}
