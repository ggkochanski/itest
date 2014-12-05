package org.itest.test.example9;

import org.itest.annotation.ITest;
import org.itest.annotation.ITests;

/**
 * Created by rumcajs on 10/31/14.
 */
public class ValueClassExample {
    @ITests({ @ITest(init = "A:[{class:java.lang.Integer}]", verify = "R:{@class:java.lang.Class}"),
            @ITest(init = "A:[{@class:java.lang.Integer,_:3}]", verify = "R:{@class:java.lang.Class},A:[3]") })
    public Class<?> getClass(Object o) {
        return o.getClass();
    }
}
