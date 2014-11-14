package org.itest.test.example10;

import org.itest.annotation.ITest;
import org.itest.annotation.ITestRef;
import org.itest.annotation.ITests;

/**
 * Created by rumcajs on 11/13/14.
 */
public class LazyLoadedDefinitionExample {
    @ITests(@ITest(initRef = @ITestRef(useClass = Entry.class, use = "def", assign = { "A:0=T", "A:1=T" }), verify = "R:200"))
    public int test(Entry a, Entry b) {
        return a.value + b.value;
    }
}
