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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.itest.ITestConstants;
import org.itest.ITestContext;

public class ITestContextImpl implements ITestContext {
    private final List<String> path = new ArrayList<String>();

    private final List<Object> owners = new ArrayList<Object>();

    private final Map<List<String>, List<String>> assignments = new HashMap<List<String>, List<String>>();

    private final Map<Class<?>, Map<String, String>> staticITestAssignmentMap;

    public ITestContextImpl(Map<Class<?>, Map<String, String>> staticITestAssignmentMap) {
        this.staticITestAssignmentMap = staticITestAssignmentMap;
    }

    @Override
    public void registerAssignment(String sourcePath) {
        assignments.put(new ArrayList<String>(path), normalizePath(path, Arrays.asList(StringUtils.split(sourcePath, '.'))));
    }

    private List<String> normalizePath(List<String> path, List<String> sourcePath) {
        List<String> res = new ArrayList<String>(path);
        for (int i = 0; i < sourcePath.size(); i++) {
            String s = sourcePath.get(i);
            if ( 0 == i ) {
                if ( ITestConstants.ARG.equals(s) || ITestConstants.THIS.equals(s) || ITestConstants.NULL.equals(s) ) {
                    res.clear();
                }
            }
            if ( ITestConstants.OWNER.equals(s) ) {
                res.remove(res.size() - 1);
            } else {
                res.add(s);
            }
        }
        return res;
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
        path.add(field);
        owners.add(owner);
    }

    @Override
    public void leave() {
        path.remove(path.size() - 1);
        owners.remove(owners.size() - 1);
    }

    @Override
    public int depth() {
        return path.size();
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

}
