package org.itest.test.example11;

import org.itest.ITestSuperObject;
import org.itest.annotation.ITest;
import org.itest.annotation.ITests;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by rumcajs on 11/14/14.
 */
public class ITestSuperObjectExample {
    @ITests(@ITest(init = "A:[{prop1:value1},prop1]", verify = "R:value1"))
    public Object getValue(SuperObject superObject, String name) {
        return superObject.map.get(name);
    }

    @ITests(@ITest(init = "A:[{z:z,x:x,c:c,a:a}]", verify = "R:[z,x,c,a]"))
    public Collection<String> checkOrder(SuperObject superObject) {
        return superObject.map.keySet();
    }

    static class SuperObject implements ITestSuperObject {
        private Map<String, Object> map = new LinkedHashMap<String, Object>();

        @Override
        public void setField(String name, Object value) {
            map.put(name, value);
        }
    }
}
