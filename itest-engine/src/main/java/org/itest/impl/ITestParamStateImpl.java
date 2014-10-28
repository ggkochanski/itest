package org.itest.impl;

import org.itest.param.ITestParamState;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ITestParamStateImpl implements ITestParamState {
    protected Map<String, ITestParamState> elements;

    String value;

    private Map<String, String> attributes;

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
    public Map<String, String> getAttributes() {
        return null == attributes ? emptyMap() : attributes;
    }

    private static Map<String, String> emptyMap() {
        return Collections.emptyMap();
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
