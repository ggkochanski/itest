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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.itest.ITestConfig;
import org.itest.exception.ITestMethodExecutionException;
import org.itest.param.ITestParamState;
import org.itest.verify.ITestExecutionVerifier;
import org.itest.verify.ITestFieldVerificationResult;

public class ITestExecutionVerifierImpl implements ITestExecutionVerifier {

    private final ITestConfig iTestConfig;

    public ITestExecutionVerifierImpl(ITestConfig iTestConfig) {
        this.iTestConfig = iTestConfig;
    }

    @Override
    public Collection<ITestFieldVerificationResult> verify(String name, Object itestObject, ITestParamState stateParam) {
        Collection<ITestFieldVerificationResult> res = new ArrayList<ITestFieldVerificationResult>();
        if ( null != stateParam ) {
            verify(res, name, itestObject, stateParam);
        }
        return res;
    }

    private void verify(Collection<ITestFieldVerificationResult> res, String name, Object resultObject, ITestParamState stateParam) {
        boolean testResult = false;
        try {
            if ( null == stateParam.getNames() ) {
                if ( null == stateParam.getValue() ) {
                    testResult = (null == resultObject);
                    res.add(new ITestFieldVerificationResultImpl(name, null, resultObject, testResult, null));
                } else if ( null == resultObject ) {
                    res.add(new ITestFieldVerificationResultImpl(name, stateParam.getValue(), null, false, null));
                } else {
                    Object expectedValue = iTestConfig.getITestValueConverter().convert(resultObject.getClass(), stateParam.getValue());
                    testResult = expectedValue.equals(resultObject);
                    res.add(new ITestFieldVerificationResultImpl(name, stateParam.getValue(), resultObject, testResult, null));
                }
            } else if ( resultObject instanceof Collection ) {
                Iterable<String> fNames = stateParam.getNames();
                if ( null != fNames ) {
                    List<Object> list = new ArrayList<Object>((Collection<Object>) resultObject);
                    for (String fName : stateParam.getNames()) {
                        int index = Integer.parseInt(fName);
                        if ( index >= list.size() ) {
                            res.add(new ITestFieldVerificationResultImpl(name + ".size()", Integer.valueOf(index + 1), Integer.valueOf(list.size()), false,
                                    null));
                        } else {
                            verify(res, name + "." + fName, list.get(index), stateParam.getElement(fName));
                        }
                    }
                }
            } else {
                Iterable<String> fNames = stateParam.getNames();
                if ( null != fNames ) {
                    for (String fName : stateParam.getNames()) {
                        try {
                            verify(res, name + "." + fName, getField(resultObject, fName), stateParam.getElement(fName));
                        } catch (Exception e) {
                            res.add(new ITestFieldVerificationResultImpl(name + "." + fName, stateParam.getElement(fName), null, false, e.getMessage()));
                        }
                    }
                }
                // throw new ITestVerificationException("Type (" + itestObject.getClass().getName() + ") not recognized (" + itestObject + ")", null);
            }
        } catch (ClassCastException e) {
            res.add(new ITestFieldVerificationResultImpl(name, stateParam.getValue(), resultObject, false, e.getMessage()));
        }
    }

    private Object getField(Object itestObject, String name) {
        Field field = null;
        Class<?> clazz = itestObject.getClass();
        do {
            try {
                field = clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                // do nothing, search in superclass
            } catch (SecurityException e) {
                throw new ITestMethodExecutionException("Security Exception", e);
            }
        } while (null != (clazz = clazz.getSuperclass()));
        if ( null == field ) {
            throw new ITestMethodExecutionException("Field(" + name + ") not found in " + itestObject.getClass().getName(), null);
        }
        field.setAccessible(true);
        try {
            return field.get(itestObject);
        } catch (Exception e) {
            throw new ITestMethodExecutionException("Getting field(" + name + ") error.", e);
        }
    }

    static class ITestFieldVerificationResultImpl implements ITestFieldVerificationResult {

        private final String name;

        private final Object expectedResult;

        private final Object actualResult;

        private final boolean success;

        private final String message;

        public ITestFieldVerificationResultImpl(String name, Object expectedResult, Object actualResult, boolean success, String message) {
            this.name = name;
            this.expectedResult = expectedResult;
            this.actualResult = actualResult;
            this.success = success;
            this.message = message;
        }

        @Override
        public boolean isSuccess() {
            return success;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(success ? "Success" : "Failure").append(" ").append(name).append(".");
            if ( !success ) {
                sb.append(" Expected: ").append(expectedResult).append(" acutal: ").append(actualResult);
            }
            if ( null != message ) {
                sb.append(" (").append(message).append(")");
            }
            return sb.toString();
        }
    }

}
