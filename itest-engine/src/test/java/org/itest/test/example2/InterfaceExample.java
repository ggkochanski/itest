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
package org.itest.test.example2;

import java.util.Date;

import org.itest.annotation.ITest;
import org.itest.annotation.ITestFieldImpl;
import org.itest.annotation.ITests;

public class InterfaceExample {
    @ITestFieldImpl(MyInterfaceImpl.class)
    private MyInterface<Date> myInterface;

    @ITests({
            @ITest(name = "dynamic interface", init = "T:{myInterface:{myInterfaceAction(*):1000}},A:[0]", verify = "R:'Thu Jan 01 01:00:01 CET 1970'"),
            @ITest(name = "interface specific impl", init = "T:{myInterface:{class:org.itest.test.example2.MyInterfaceImpl2}},A:[1000]", verify = "R:'Thu Jan 01 00:59:59 CET 1970'"),
            @ITest(name = "interface default impl", init = "A:[1000]", verify = "R:'Thu Jan 01 01:00:01 CET 1970'") })
    public String testMethod(Date a) {
        return myInterface.myInterfaceAction(a).toString();
    }
}
