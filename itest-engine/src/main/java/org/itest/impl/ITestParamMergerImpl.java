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
import org.itest.json.simple.ITestSimpleJsonState;
import org.itest.param.ITestParamAssignment;
import org.itest.param.ITestParamMerger;
import org.itest.param.ITestParamState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;

public class ITestParamMergerImpl implements ITestParamMerger {

    @Override
    public ITestParamState merge(ITestParamAssignment... itestParamAssignments) {
        Collection<ITestParamState> unifiedStates = unifyStates(itestParamAssignments);
        ITestParamState mergedState = mergeUnified(unifiedStates);
        return mergedState;
    }

    private Collection<ITestParamState> unifyStates(ITestParamAssignment... iTestParamAssignments) {
        Collection<ITestParamState> unifiedStates = new ArrayList<ITestParamState>();
        for (ITestParamAssignment iTestParamAssignment : iTestParamAssignments) {
            ITestParamState itestParam = iTestParamAssignment.getITestParamState();
            for (String transformation : iTestParamAssignment.getTransformation()) {
                //ITestParamStateImpl state = new ITestParamStateImpl();
                ITestParamState state = getState(transformation, itestParam);
                unifiedStates.add(state);
            }
        }
        return unifiedStates;
    }

    private ITestParamState mergeUnified(Collection<ITestParamState> unifiedStates) {
        if ( 1 == unifiedStates.size() ) {
            return unifiedStates.iterator().next();
        }
        Collection<String> collectedElements = collectAllNames(unifiedStates);
        ITestParamStateImpl mergedState = new ITestParamStateImpl();
        for (String element : collectedElements) {
            merge(element, mergedState, unifiedStates);
        }
        return mergedState;
    }

    private void merge(String name, ITestParamStateImpl parentState, Collection<ITestParamState> unifiedStates) {
        ITestParamStateImpl currentState = null;
        Collection<ITestParamState> unifiedElements = new ArrayList<ITestParamState>(unifiedStates.size());
        for (ITestParamState itestState : unifiedStates) {
            unifiedElements.add(null == itestState ? null : itestState.getElement(name));
        }
        Iterable<String> allNamesIterable = collectAllNames(unifiedElements);
        if ( null != allNamesIterable ) {
            currentState = new ITestParamStateImpl();
            currentState.elements = ITestSimpleJsonState.createElements();
            copyAttributes(currentState, unifiedElements);
            parentState.addElement(name, currentState);
            for (String elementName : allNamesIterable) {
                merge(elementName, currentState, unifiedElements);
            }
        } else {
            for (ITestParamState itestState : unifiedElements) {
                if ( null != itestState ) {
                    if ( null == currentState ) {
                        currentState = new ITestParamStateImpl();
                        parentState.addElement(name, currentState);
                        copyAttributes(currentState, unifiedElements);
                    }
                    if(null==itestState.getNames()) {
                        currentState.value = itestState.getValue();
                    }
                }
            }
        }
    }

    private void copyAttributes(ITestParamStateImpl currentState, Collection<ITestParamState> unifiedElements) {
        for (ITestParamState unified : unifiedElements) {
            if ( null == unified ) {
                continue;
            }
            Iterable<String> attributeNames = unified.getAttributeNames();
            if ( null != attributeNames ) {
                for (String attributeName : attributeNames) {
                    currentState.addAttribute(attributeName, unified.getAttribute(attributeName));
                }
            }
        }
    }

    private Collection<String> collectAllNames(Collection<ITestParamState> unifiedElements) {
        Collection<String> res = null;
        for (ITestParamState state : unifiedElements) {
            if ( null != state ) {
                Collection<String> names = state.getNames();
                if ( names != null ) {
                    if ( null == res ) {
                        res = new LinkedHashSet<String>();
                    }
                    res.addAll(names);
                }
            }
        }
        return res;
    }

    private ITestParamState getState(String transformation, ITestParamState itestParam) {
        if ( 0 == transformation.length() ) {
            return itestParam;
        }
        String s = transformation.replaceAll(ITestConstants.ASSIGN, ITestConstants.ASSIGN_SEPARATOR + ITestConstants.ASSIGN + ITestConstants.ASSIGN_SEPARATOR);
        StringTokenizer t = new StringTokenizer(s, ITestConstants.ASSIGN_SEPARATOR);
        ITestParamStateImpl res = new ITestParamStateImpl();
        ITestParamStateImpl prevState = null;
        String token;
        String prevToken = null;
        while (!ITestConstants.ASSIGN.equals(token = t.nextToken())) {
            ITestParamStateImpl p = new ITestParamStateImpl();
            if (prevState != null) {
                prevState.addElement(prevToken, p);
            } else {
                p = res;
            }
            prevState = p;
            prevToken = token;
        }
        while (t.hasMoreElements()) {
            itestParam = itestParam.getElement(t.nextToken());
        }
        prevState.addElement(prevToken, itestParam);
        return res;
    }
}
