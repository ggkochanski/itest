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
package org.itest.test.example4;

import org.itest.annotation.ITest;
import org.itest.annotation.ITestRef;
import org.itest.annotation.ITests;

import java.util.List;
import java.util.Map;

public class CollectionsExample {
    private long[] array;

    private List<Double> list;

    private Map<String, String> map;

    @ITests({ @ITest(name = "arrayInit", init = "T:{array:[1,2,3]}"),
            @ITest(name = "verify1stElement", initRef = @ITestRef(use = "arrayInit"), verify = "T:{array:[1]}"),
            @ITest(name = "verify2ndElement", initRef = @ITestRef(use = "arrayInit"), verify = "T:{array:[{},2]}") })
    public int getArrayLength() {
        return array.length;
    }

    @ITests({
            @ITest(name = "listInit", init = "T:{list:[1,15.27,.7e-2]}"), //
            @ITest(name = "verifyElementValueV1", initRef = @ITestRef(use = "listInit"), verify = "T:{list:{2:7e-3}}"),
            @ITest(name = "verifyElementValueV2", initRef = @ITestRef(use = "listInit"), verify = "T:{list:[{},{},7e-3]}") })
    public int getListSize() {
        return list.size();
    }

    @ITests({ @ITest(name = "mapInit", init = "T:{map:[{key:key1,value:value1},{key:key2}]}"),
            @ITest(name = "checkValueExists", initRef = @ITestRef(use = "mapInit"), verify = "T:{map:[{key:key1,value:value1},{key:xxx,value:null}]}"),
            @ITest(name = "checkKeyExists", initRef = @ITestRef(use = "mapInit"), verify = "T:{map:[{key:key1},{key:key2}]}") })
    public String getFromMap(String key) {
        return map.get(key);
    }

    @ITests({ @ITest(name = "allInit", initRef = { @ITestRef(use = "arrayInit"), @ITestRef(use = "listInit"), @ITestRef(use = "mapInit") }),
            @ITest(name = "allTest", initRef = @ITestRef(use = "allInit"), verify = "R:8") })
    public int getAllSize() {
        return array.length + list.size() + map.size();
    }
}
