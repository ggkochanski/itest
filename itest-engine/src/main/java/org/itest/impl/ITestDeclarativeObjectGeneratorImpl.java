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
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import org.itest.ITestConfig;
import org.itest.ITestContext;
import org.itest.annotation.ITestField;
import org.itest.param.ITestParamState;

public class ITestDeclarativeObjectGeneratorImpl extends ITestRandomObjectGeneratorImpl {

    public ITestDeclarativeObjectGeneratorImpl(ITestConfig iTestConfig) {
        super(iTestConfig);
    }

    @Override
    public <T> T generateRandom(Class<T> clazz, ITestParamState iTestState, Map<String, Type> itestGenericMap, ITestContext iTestContext) {
        if ( null == iTestState && !clazz.isPrimitive() ) {
            return null;
        }
        return super.generateRandom(clazz, iTestState, itestGenericMap, iTestContext);
    }

    @Override
    protected Object fillCollection(Object o, Type type, ITestParamState iTestState, Map<String, Type> map, ITestContext iTestContext) {
        if ( null == iTestState || null == iTestState.getSizeParam() ) {
            return o;
        }
        return super.fillCollection(o, type, iTestState, map, iTestContext);
    }

    @Override
    protected Object fillMap(Object o, Type type, ITestParamState iTestState, Map<String, Type> map, ITestContext iTestContext) {
        if ( null == iTestState || null == iTestState.getSizeParam() ) {
            return o;
        }
        return super.fillMap(o, type, iTestState, map, iTestContext);
    }

    @Override
    protected void fillField(Field f, Object o, ITestParamState fITestState, Map<String, Type> map, ITestContext iTestContext) {
        if ( null == fITestState && !iTestContext.isStaticAssignmentRegistered(o.getClass(), f.getName()) && !f.isAnnotationPresent(ITestField.class) ) {
            return;
        }
        super.fillField(f, o, fITestState, map, iTestContext);
    }

    @Override
    protected void fillMethod(Method m, Object res, ITestParamState mITestState, Map<String, Type> map, ITestContext iTestContext,
            Map<String, Object> methodResults) {
        if ( null == mITestState ) {
            return;
        }
        super.fillMethod(m, res, mITestState, map, iTestContext, methodResults);
    }
}
