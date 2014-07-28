package org.itest.impl;

import java.util.HashMap;
import java.util.Map;

import org.itest.param.ITestParamState;

public class ITestParamStateImpl implements ITestParamState {
    protected Map<String, ITestParamState> elements;

    String value;

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
