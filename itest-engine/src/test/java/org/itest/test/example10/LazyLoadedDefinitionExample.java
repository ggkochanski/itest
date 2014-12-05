package org.itest.test.example10;

import org.itest.annotation.ITest;
import org.itest.annotation.ITestRef;
import org.itest.annotation.ITests;

/**
 * Created by rumcajs on 11/13/14.
 */
public class LazyLoadedDefinitionExample {
    @ITests({
            @ITest(initRef = @ITestRef(useClass = Entry.class, use = "def", assign = {"A:0=T", "A:1=T"}), verify = "R:200"),
            @ITest(name="t1",initRef = {@ITestRef(useClass = Entry.class, use = "e1", assign = "A:0=T"),
                    @ITestRef(useClass = Entry.class, use = "def", assign = "A:1=T")}, verify = "R:200,A:[{value:100,value2:2},{value:100,value2:3}]"),
            @ITest(initRef = @ITestRef(use = "t1"),verify = "A:[{o:{@class:java.util.Date,_:7}},{o:{@class:java.lang.Long,_:6},o2:{@class:java.lang.Integer,_:2}}]")
    })
    public int test(Entry a, Entry b) {
        return a.value + b.value;
    }
    @ITests(@ITest(initRef =@ITestRef(use="t1"),verify = "A:[{e:{value:1,value2:2}},{e:{value:1,value2:3}}]"))
    public void test2(Entry a,Entry b){

    }


}
