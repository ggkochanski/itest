package org.itest.impl;

import org.itest.param.ITestParamState;

import java.util.*;

public class ITestParamStateImpl implements ITestParamState {
    protected Map<String, ITestParamState> elements;

    String value;

    private Map<String, String> attributes;

    public ITestParamStateImpl(){}
    public ITestParamStateImpl(ITestParamState paramState) {
        this.value = paramState.getValue();
        Iterable<String> paramNames = paramState.getNames();
        if (null != paramNames) {
            for (String paramName : paramNames) {
                addElement(paramName, paramState.getElement(paramName));
            }
        }
        Iterable<String> attributeNames = paramState.getAttributeNames();
        if ( null != attributeNames ) {
            for (String attributeName : attributeNames) {
                addAttribute(attributeName, paramState.getAttribute(attributeName));
            }
        }
    }

    public void addAttribute(String key, String value) {
        if(null==attributes){
            attributes=new HashMap<String, String>();
        }
        attributes.put(key,value);
    }

    @Override
    public Integer getSizeParam() {
        return null == elements ? null : elements.size();
    }

    public void addElement(String token, ITestParamState iTestParamsImpl) {
        if (null == elements) {
            elements = createElements();
        }
        elements.put(token, iTestParamsImpl);
    }

    @Override
    public Collection<String> getNames() {
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
    public String getAttribute(String name) {
        return null == attributes ? null : attributes.get(name);
    }

    public Iterable<String> getAttributeNames() {
        return null == attributes ? null : attributes.keySet();
    }

    private static Map<String, String> emptyMap() {
        return Collections.emptyMap();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (null == elements) {
            sb.append(":").append(getValue());
        } else {
            sb.append(elements);
        }
        return sb.toString();
    }
    private static Map<String,ITestParamState> createElements(){
        return new LinkedHashMap<String, ITestParamState>();
    }
}
