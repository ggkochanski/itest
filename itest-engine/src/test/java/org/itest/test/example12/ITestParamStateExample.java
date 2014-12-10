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

    @ITests({@ITest(init = "A:[null]", verify = "A:[{elements:null}]"),
            @ITest(init = "A:[{_:null}]", verify = "A:[{elements:null}]")})
    public void nullStateTest(ITestParamState s) {
        Assert.assertNotNull(s);
        Assert.assertNull(s.getNames());
        Assert.assertNull(s.getValue());
    }

    @ITests({@ITest(init = "A:[{}]", verify = "A:[{elements:{}}]")})
    public void emptyStateTest(ITestParamState s) {
        Assert.assertNotNull(s);
        Assert.assertNotNull(s.getNames());
        Assert.assertNull(s.getValue());
    }

    @ITests(@ITest(init = "A:[{s:null}]", verify = "A:[{s:{elements:null}}]"))
    public void nullStateTestInClass(SomeClass c) {
        Assert.assertNotNull(c.s);
        Assert.assertNull(c.s.getNames());
        Assert.assertNull(c.s.getValue());
    }
    @ITests(@ITest(init = "A:[{s:{}}]", verify = "A:[{s:{elements:{}}}]"))
    public void emptyStateTestInClass(SomeClass c) {
        Assert.assertNotNull(c.s);
        Assert.assertNotNull(c.s.getNames());
        Assert.assertNull(c.s.getValue());
    }

    static class SomeClass {
        String a;
        int[] b;
        ITestParamState s;
    }
}
