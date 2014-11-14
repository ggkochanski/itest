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
import org.itest.ITestContext;
import org.itest.annotation.ITestFieldAssignment;
import org.itest.annotation.ITestFieldClass;
import org.itest.param.ITestParamState;
import org.itest.util.reflection.ITestFieldUtil;
import org.itest.util.reflection.ITestTypeUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ITestDeclarativeObjectGeneratorImpl extends ITestRandomObjectGeneratorImpl {
    public ITestDeclarativeObjectGeneratorImpl(ITestConfig iTestConfig) {
        super(iTestConfig);
    }

    @Override
    public <T> T generateRandom(Class<T> clazz, Map<String, Type> itestGenericMap, ITestContext iTestContext) {
        if ( null == iTestContext.getCurrentParam() && !clazz.isPrimitive() ) {
            return null;
        }
        return super.generateRandom(clazz, itestGenericMap, iTestContext);
    }

    @Override
    protected Object fillCollection(Object o, Type type, Map<String, Type> map, ITestContext iTestContext) {
        if ( null == iTestContext.getCurrentParam() || null == iTestContext.getCurrentParam().getSizeParam() ) {
            return o;
        }
        return super.fillCollection(o, type, map, iTestContext);
    }

    @Override
    protected Object fillMap(Object o, Type type, Map<String, Type> map, ITestContext iTestContext) {
        if ( null == iTestContext.getCurrentParam() || null == iTestContext.getCurrentParam().getSizeParam() ) {
            return o;
        }
        return super.fillMap(o, type, map, iTestContext);
    }

    @Override
    protected void fillField(Field f, Object o, Map<String, Type> map, ITestContext iTestContext) {
        ITestParamState fITestState = iTestContext.getCurrentParam();
        if ( null == fITestState && !iTestContext.isStaticAssignmentRegistered(o.getClass(), f.getName()) && !f.isAnnotationPresent(ITestFieldAssignment.class)
                && !f.isAnnotationPresent(ITestFieldClass.class) ) {
            return;
        }
        super.fillField(f, o, map, iTestContext);
    }

    @Override
    protected void fillMethod(Method m, Object res, String mSignature, Map<String, Type> map, ITestContext iTestContext, Map<String, Object> methodResults) {
        if ( null == iTestContext.getCurrentParam() ) {
            return;
        }
        super.fillMethod(m, res, mSignature, map, iTestContext, methodResults);
    }

    @Override
    protected Collection<ITestFieldUtil.FieldHolder> collectFields(Class<?> clazz, Map<String, Type> map, ITestContext iTestContext) {
        ITestParamState iTestParamState = iTestContext.getCurrentParam();
        Collection<ITestFieldUtil.FieldHolder> res = Collections.EMPTY_LIST;
        if ( null != iTestParamState && null != iTestParamState.getNames() ) {
            res = new ArrayList<ITestFieldUtil.FieldHolder>();
            List<String> names = new ArrayList<String>(iTestParamState.getNames());
            if ( 0 == names.size() ) {
                names.add("");
            }
            for (String name : names) {
                Class<?> t = clazz;
                do {
                    for (Field f : t.getDeclaredFields()) {
                        if ( null != f.getAnnotation(ITestFieldClass.class) ) {
                            res.add(new ITestFieldUtil.FieldHolder(f, map));
                        } else if ( f.getName().equals(name) ) {
                            res.add(new ITestFieldUtil.FieldHolder(f, map));
                        }
                    }
                    map = ITestTypeUtil.getTypeMap(t, map);
                } while ((t = t.getSuperclass()) != null);
            }
        } else {
            res = super.collectFields(clazz, map, iTestContext);
        }
        return res;
    }
}
