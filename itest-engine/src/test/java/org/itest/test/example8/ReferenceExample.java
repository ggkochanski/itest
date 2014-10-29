package org.itest.test.example8;

import org.itest.annotation.ITest;
import org.itest.annotation.ITests;

/**
 * Created by rumcajs on 10/29/14.
 */
public class ReferenceExample {
    @ITests(@ITest(init = "A:[{b:{a:{@ref:../..}}},{@ref:../0/b}]", verify = "R:null"))
    void test(A a, B b) {
        assert a.b.a == a;
        assert b.a.b == b;
        assert a.b == b;
        assert b.a == a;
    }

    static class A {
        B b;
    }

    static class B {
        A a;
    }
}
