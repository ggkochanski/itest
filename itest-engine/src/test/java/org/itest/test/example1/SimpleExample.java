/**
 * <pre>
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 Grzegorz Kochański
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * </pre>
 */
package org.itest.test.example1;

import org.itest.annotation.ITest;
import org.itest.annotation.ITestRef;
import org.itest.annotation.ITests;
import org.junit.Assert;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

public class SimpleExample {
    @ITests(@ITest(init = "A:[3,16]", verify = "R:19"))
    public int sum(int a, int b) {
        return a + b;
    }

    @ITests({ @ITest(name = "max1", init = "A:[{x:[1,22,333]}]", verify = "R:3"), //
            @ITest(name = "max2", init = "A:[{x:[1,22,333,4444]}]", verify = "R:4") })
    public int max(Entity<String> e) {
        int res = 0;
        for (String s : e.x) {
            res = Math.max(res, s.length());
        }
        return res;
    }

    private Entity<Object> e;

    @ITests({ @ITest(initRef = @ITestRef(use = "max1", assign = "T:e=A:0"), verify = "R:3,T:{e:{x:[1,{},333]}}"), //
            @ITest(init = "T:{e:{x:[{},{}]}}", verify = "R:2") })
    public int size() {
        return e.x.size();
    }

    @ITests({ @ITest(init = "A:[aaa]", verify = "R:aaa") //
    // ,@ITest(init = "A:[aaa]", verify = "R:null") - fixed
    })
    public String test(String a) {
        return a;
    }

    static class Entity<X> {

        private final List<X> x;

        public Entity(int a, List<X> x) {
            this.x = x;
        }
    }

    enum SomeEnum{
        A,B,C;
    }
    static class SomeClass<T>{
        T value;
    }
    @ITests(@ITest(init = "A:[{value:B}]",verify = "R:B"))
    public SomeEnum getEnum(SomeClass<SomeEnum> e){
        return e.value;
    }

    @ITests(@ITest(init = "A:[1,2,3,4,{@class:java.sql.Time,_:5}]", verify = "A:[{},{@class:java.sql.Time}],R:15"))
    public long setDate(Timestamp timestamp, Time time, Date sqlDate, java.util.Date utilDate, java.util.Date anyDate) {
        Assert.assertEquals(Timestamp.class,timestamp.getClass());
        Assert.assertEquals(Time.class, time.getClass());
        Assert.assertEquals(Date.class, sqlDate.getClass());
        Assert.assertEquals(java.util.Date.class, utilDate.getClass());
        return timestamp.getTime() + time.getTime() + sqlDate.getTime() + utilDate.getTime()+anyDate.getTime();
    }
    @ITests(@ITest(init="A:[{value:[a,b,c],offset:0,count:3}]",verify = "R:abc"))
    public String setString(String s){
        return s;
    }
}
