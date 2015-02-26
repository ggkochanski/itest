package org.itest.test.issue;

import org.itest.annotation.ITest;
import org.itest.annotation.ITests;

/**
 * Created by rumcajs on 2/26/15.
 */
public class ITestJsonEscapeTest {
    @ITests({
            @ITest(init = "A:[{s:\"abc\"}]",verify = "A:[{s:{count:3}}]"),
            @ITest(init = "A:[{s:\"a'bc\"}]",verify = "A:[{s:{count:4}}]"),
            @ITest(init = "A:[{s:\"a\\'bc\"}]",verify = "A:[{s:{count:4}}]"),
            @ITest(init = "A:[{s:\"a\\\'bc\"}]",verify = "A:[{s:{count:4}}]"),
            @ITest(init = "A:[{s:\"a\\\"bc\"}]",verify = "A:[{s:{count:4}}]"),
    })
    public void test(C c){

    }

    static class C{
        String s;
    }

}
