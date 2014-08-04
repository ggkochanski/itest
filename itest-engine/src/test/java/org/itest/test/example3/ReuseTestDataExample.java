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
package org.itest.test.example3;

import org.itest.annotation.ITest;
import org.itest.annotation.ITestRef;
import org.itest.annotation.ITests;

public class ReuseTestDataExample {
    private String s;

    @ITests({
            @ITest(name = "t1", init = "T:{s:abcdbcdcdd},A:[a]", verify = "R:1"), //
            @ITest(initRef = @ITestRef(use = "t1"), init = "A:[b]", verify = "R:2"),
            @ITest(initRef = @ITestRef(use = "t1"), init = "T:{s:aaa}", verify = "R:3") })
    public int countChar(char c) {
        int res = 0;
        for (int i = 0; i < s.length(); i++) {
            if ( c == s.charAt(i) ) {
                res++;
            }
        }
        return res;
    }

}
