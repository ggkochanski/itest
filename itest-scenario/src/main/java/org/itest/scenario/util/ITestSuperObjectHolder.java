package org.itest.scenario.util;

import org.itest.ITestSuperObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rumcajs on 11/18/14.
 */
public class ITestSuperObjectHolder<T> implements ITestSuperObject<T> {
    private Map<String, Object> map = new HashMap<String, Object>();

    @Override
    public void setField(String name, Object value) {
        map.put(name, value);
    }

    public Object getField(String name) {
        return map.get(name);
    }
}
