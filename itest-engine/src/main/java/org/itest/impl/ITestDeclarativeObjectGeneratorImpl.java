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

import com.google.common.reflect.TypeToken;
import org.itest.ITestConfig;
import org.itest.ITestContext;
import org.itest.annotation.ITestFieldAssignment;
import org.itest.annotation.ITestFieldClass;
import org.itest.param.ITestParamState;
import org.itest.util.reflection.ITestFieldProvider;
import org.itest.util.reflection.ITestFieldProvider.FieldHolder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ITestDeclarativeObjectGeneratorImpl extends ITestRandomObjectGeneratorImpl {
    public ITestDeclarativeObjectGeneratorImpl(ITestConfig iTestConfig) {
        super(iTestConfig);
    }

    @Override
    public <T> T generateRandom(Class<T> clazz, ITestContext iTestContext) {
        if ( null == iTestContext.getCurrentParam() && !clazz.isPrimitive() ) {
            return null;
        }
        return super.generateRandom(clazz, iTestContext);
    }

    @Override
    protected Object fillCollection(Object o, TypeToken type, ITestContext iTestContext) {
        if ( null == iTestContext.getCurrentParam() || null == iTestContext.getCurrentParam().getSizeParam() ) {
            return o;
        }
        return super.fillCollection(o, type, iTestContext);
    }

    @Override
    protected Object fillMap(Object o, TypeToken type, ITestContext iTestContext) {
        if ( null == iTestContext.getCurrentParam() || null == iTestContext.getCurrentParam().getSizeParam() ) {
            return o;
        }
        return super.fillMap(o, type, iTestContext);
    }

    @Override
    protected void fillField(Type fType, Field f, Object o, ITestContext iTestContext) {
        ITestParamState fITestState = iTestContext.getCurrentParam();
        if ( null == fITestState && !iTestContext.isStaticAssignmentRegistered(o.getClass(), f.getName()) && !f.isAnnotationPresent(ITestFieldAssignment.class)
                && !f.isAnnotationPresent(ITestFieldClass.class) ) {
            return;
        }
        super.fillField(fType, f, o, iTestContext);
    }

    @Override
    protected void fillMethod(Type mType, Method m, Object res, String mSignature, ITestContext iTestContext, Map<String, Object> methodResults) {
        if ( null == iTestContext.getCurrentParam() ) {
            return;
        }
        super.fillMethod(mType, m, res, mSignature, iTestContext, methodResults);
    }

    @Override
    protected Collection<ITestFieldProvider.FieldHolder> collectFields(Type type, ITestContext iTestContext) {
        Collection<ITestFieldProvider.FieldHolder> res = Collections.EMPTY_LIST;
        ITestParamState iTestParamState = iTestContext.getCurrentParam();

        if ( null != iTestParamState && null != iTestParamState.getNames() ) {
            res = new ArrayList<ITestFieldProvider.FieldHolder>();
            Collection<FieldHolder> allFields = super.collectFields(type, iTestContext);
            for (FieldHolder fieldHolder : allFields) {
                if (null != fieldHolder.getField().getAnnotation(ITestFieldClass.class)
                        || null != iTestParamState.getElement(fieldHolder.getField().getName())) {
                    res.add(fieldHolder);
                }
            }
        }
        return res;
    }
}
