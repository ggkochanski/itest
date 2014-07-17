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
package org.itest.test.example;

import java.util.List;

import org.itest.annotation.ITest;
import org.itest.annotation.ITestInitRef;
import org.itest.annotation.ITests;

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

    @ITests({ @ITest(initRef = @ITestInitRef(use = "max1", assign = "T.e=A.0"), verify = "R:3,T:{e:{x:[1,{},333]}}"), //
            @ITest(init = "T:{e:{x:[{},{}]}}", verify = "R:2") })
    public int size() {
        return e.x.size();
    }

    static class Entity<X> {

        private final List<X> x;

        public Entity(int a, List<X> x) {
            this.x = x;
        }
    }

}
