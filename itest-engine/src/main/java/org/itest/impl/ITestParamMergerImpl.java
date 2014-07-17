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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.itest.ITestConstants;
import org.itest.param.ITestParamMerger;
import org.itest.param.ITestParamState;

public class ITestParamMergerImpl implements ITestParamMerger {

    @Override
    public ITestParamState merge(Collection<String> transformations, Collection<ITestParamState> itestParams) {
        Collection<ITestParamState> unifiedStates = unifyStates(transformations, itestParams);

        ITestParamState mergedState = mergeUnified(unifiedStates);

        return mergedState;
    }

    private Collection<ITestParamState> unifyStates(Collection<String> transformations, Collection<ITestParamState> itestParams) {
        Collection<ITestParamState> unifiedStates = new ArrayList<ITestParamState>();
        Iterator<ITestParamState> itestParamsIterator = itestParams.iterator();
        for (String transformation : transformations) {
            ITestParamState itestParam = itestParamsIterator.next();
            ITestStateImpl state = new ITestStateImpl();
            ITestParamState thisState = getState(ITestConstants.THIS, transformation, itestParam);
            ITestParamState argState = getState(ITestConstants.ARG, transformation, itestParam);
            if ( null != thisState ) {
                state.addElement(ITestConstants.THIS, thisState);
            }
            if ( null != argState ) {
                state.addElement(ITestConstants.ARG, argState);
            }
            unifiedStates.add(state);
        }
        return unifiedStates;
    }

    private ITestParamState mergeUnified(Collection<ITestParamState> unifiedStates) {
        ITestStateImpl mergedState = new ITestStateImpl();
        merge(ITestConstants.THIS, mergedState, unifiedStates);
        merge(ITestConstants.ARG, mergedState, unifiedStates);
        return mergedState;
    }

    private void merge(String name, ITestStateImpl parentState, Collection<ITestParamState> unifiedStates) {
        ITestStateImpl currentState = null;
        Collection<ITestParamState> unifiedElements = new ArrayList<ITestParamState>(unifiedStates.size());
        for (ITestParamState itestState : unifiedStates) {
            unifiedElements.add(null == itestState ? null : itestState.getElement(name));
        }
        Iterable<String> allNamesIterable = getAllNamesInterable(unifiedElements);
        if ( null != allNamesIterable ) {
            currentState = new ITestStateImpl();
            currentState.elements = new HashMap<String, ITestParamState>();
            parentState.addElement(name, currentState);
            for (String elementName : allNamesIterable) {
                merge(elementName, currentState, unifiedElements);
            }
        } else {
            for (ITestParamState itestState : unifiedElements) {
                if ( null != itestState ) {
                    if ( null == currentState ) {
                        currentState = new ITestStateImpl();
                        parentState.addElement(name, currentState);
                    }
                    currentState.value = itestState.getValue();
                }
            }
        }
    }

    private Iterable<String> getAllNamesInterable(Collection<ITestParamState> unifiedElements) {
        Collection<String> res = null;
        for (ITestParamState state : unifiedElements) {
            if ( null != state ) {
                Iterable<String> names = state.getNames();
                if ( names != null ) {
                    if ( null == res ) {
                        res = new HashSet<String>();
                    }
                    for (String name : names) {
                        res.add(name);
                    }
                }
            }
        }
        return res;
    }

    private ITestParamState getState(String element, String transformation, ITestParamState itestParam) {
        if ( 0 == transformation.length() ) {
            return itestParam.getElement(element);
        }
        StringTokenizer t = new StringTokenizer(transformation.replaceAll(ITestConstants.ASSIGN, "." + ITestConstants.ASSIGN + "."), ".");
        String token = t.nextToken();
        if ( element.equals(token) ) {
            ITestStateImpl prevState = new ITestStateImpl();
            ITestParamState res = prevState;
            String prevToken = null;
            while ( !ITestConstants.ASSIGN.equals(token = t.nextToken())) {
                if ( null != prevToken ) {
                    ITestStateImpl p = new ITestStateImpl();
                    prevState.addElement(prevToken, p);
                    prevState = p;
                }
                prevToken = token;
            }
            while (t.hasMoreElements()) {
                itestParam = itestParam.getElement(t.nextToken());
            }
            if ( null == prevToken ) {
                res = itestParam;
            } else {
                prevState.addElement(prevToken, itestParam);
            }
            return res;
        }
        return null;
    }

    static class ITestStateImpl implements ITestParamState {
        private Map<String, ITestParamState> elements;

        private String value;

        @Override
        public Integer getSizeParam() {
            return null == elements ? null : elements.size();
        }

        public void addElement(String token, ITestParamState iTestParamsImpl) {
            if ( null == elements ) {
                elements = new HashMap<String, ITestParamState>();
            }
            elements.put(token, iTestParamsImpl);
        }

        @Override
        public Iterable<String> getNames() {
            return elements == null ? null : elements.keySet();
        }

        @Override
        public ITestParamState getElement(String name) {
            return elements == null ? null : elements.get(name);
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if ( null == elements ) {
                sb.append(":").append(getValue());
            } else {
                sb.append(elements);
            }
            return sb.toString();
        }
    }
}
