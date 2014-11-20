package org.itest.scenario;

import org.itest.ITestSuperObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rumcajs on 11/18/14.
 */
public class ITestScenarioParamsHolder implements ITestSuperObject {
    private Map<String, Object> map = new HashMap<String, Object>();

    @Override
    public void setField(String name, Object value) {
        map.put(name, value);
    }

    public Object getField(String name) {
        return map.get(name);
    }
}
