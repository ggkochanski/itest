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

import org.itest.ITestConstants;
import org.itest.ITestContext;
import org.itest.exception.ITestException;
import org.itest.param.ITestParamState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class ITestContextImpl implements ITestContext {
    private final List<String> path = new ArrayList<String>();

    private final List<Object> owners = new ArrayList<Object>();

    private final List<ITestParamState> params = new ArrayList<ITestParamState>();

    private final Map<List<String>, List<String>> assignments = new HashMap<List<String>, List<String>>();

    private final Map<Class<?>, Map<String, String>> staticITestAssignmentMap;

    private final ITestParamState rootParam;

    private final ITestValueHolder rootValueHolder;

    private final List<ITestValueHolder> valueHolders = new ArrayList<ITestValueHolder>();

    public ITestContextImpl(ITestParamState rootParam, Map<Class<?>, Map<String, String>> staticITestAssignmentMap) {
        this.rootParam = rootParam;
        this.params.add(rootParam);
        this.rootValueHolder = new ITestValueHolder();
        this.valueHolders.add(rootValueHolder);
        this.staticITestAssignmentMap = staticITestAssignmentMap;
    }

    @Override
    public void registerAssignment(String sourcePath) {
        // assignments.put(new ArrayList<String>(path), normalizePath(path, Arrays.asList(StringUtils.split(sourcePath, '.'))));
    }

    @Override
    public void registerAssignment(Class<?> clazz, String name) {
        registerAssignment(getStaticAssignment(clazz, name));
    }

    @Override
    public boolean isStaticAssignmentRegistered(Class<?> clazz, String field) {
        return null != getStaticAssignment(clazz, field);
    }

    private String getStaticAssignment(Class<?> clazz, String field) {
        Map<String, String> fieldMap = staticITestAssignmentMap.get(clazz);
        return null == fieldMap ? null : fieldMap.get(field);
    }

    @Override
    public void enter(Object owner, String field) {
        getCurrentValueHolder().setValue(owner);
        params.add(null == getCurrentParam() ? null : getCurrentParam().getElement(field));
        ITestValueHolder vh = new ITestValueHolder();
        getCurrentValueHolder().addEelement(field, vh);
        valueHolders.add(vh);
        path.add(field);
        owners.add(owner);
    }

    public void setEmptyParam() {
        params.set(params.size() - 1, ITestRandomObjectGeneratorImpl.EMPTY_STATE);
    }

    @Override
    public void leave(Object value) {
        if ( getCurrentValueHolder().valueSet ) {
            assert getCurrentValueHolder().value == value;
        }
        getCurrentValueHolder().setValue(value);
        valueHolders.remove(valueHolders.size() - 1);
        params.remove(params.size() - 1);
        path.remove(path.size() - 1);
        owners.remove(owners.size() - 1);
    }

    @Override
    public int depth() {
        return path.size();
    }

    private ITestValueHolder getCurrentValueHolder() {
        return valueHolders.get(valueHolders.size() - 1);
    }

    static class ITestAssignment {
        List<String> source;

        List<String> target;
    }

    @Override
    public Map<List<String>, List<String>> getAssignments() {
        return assignments;
    }

    @Override
    public Object getCurrentOwner() {
        return owners.get(owners.size() - 1);
    }

    @Override
    public String getCurrentField() {
        return path.get(path.size() - 1);
    }

    public ITestParamState getCurrentParam() {
        return params.get(params.size() - 1);
    }

    public Object findGeneratedObject(String targetPath) {
        ITestValueHolder res;
        int depth;
        if ( targetPath.startsWith(ITestConstants.SEPARATOR) ) {
            depth = 0;
        } else {
            depth = valueHolders.size() - 1;
        }
        res = valueHolders.get(depth);
        StringTokenizer st = new StringTokenizer(targetPath, ITestConstants.SEPARATOR);

        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if ( ITestConstants.PARENT.equals(token) ) {
                depth--;
                res = valueHolders.get(depth);
            } else if ( ITestConstants.NULL.equals(token) ) {
                return null;
            } else {
                res = res.getElement(token);
            }
        }
        if ( null == res ) {
            throw new ITestException("@ref:" + targetPath + " not found");
        }
        return res.getValue();
    }

    @Override
    public void replaceCurrentState(ITestParamState iTestState) {
        params.set(params.size() - 1, iTestState);
    }

    static class ITestValueHolder {
        Object value;

        Map<String, ITestValueHolder> elements;

        boolean valueSet;

        public void addEelement(String field, ITestValueHolder vh) {
            if ( null == elements ) {
                elements = new HashMap<String, ITestValueHolder>();
            }
            elements.put(field, vh);
        }

        public ITestValueHolder getElement(String token) {
            return elements.get(token);
        }

        public void setValue(Object value) {
            this.value = value;
            this.valueSet = true;
        }

        public Object getValue() {
            if ( !valueSet ) {
                throw new ITestException("Value not set");
            }
            return value;
        }
    }
}
