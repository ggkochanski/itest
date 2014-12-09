package org.itest.test.example12;

import org.itest.annotation.ITest;
import org.itest.annotation.ITests;
import org.itest.param.ITestParamState;
import org.junit.Assert;

/**
 * Created by rumcajs on 12/9/14.
 */
public class ITestParamStateExample {
    private ITestParamState state;

    @ITests(@ITest(init = "A:[{a:a,b:[0,10,20]},{@ref:../0,a:aa,b:{1:{@ref:../../../0/b/2}}}]",
            verify = "A:[{},{elements:[{key:b,value:{elements:[{key:1,value:{value:20}}]}}]}]"))
    public void setState(ITestParamState s1, ITestParamState s2) {
        Assert.assertEquals("20",s1.getElement("b").getElement("2").getValue());
        Assert.assertEquals("20",s2.getElement("b").getElement("2").getValue());
        Assert.assertEquals("20",s2.getElement("b").getElement("1").getValue());
    }

    static class SomeClass {
        String a;
        int[] b;
    }
}
