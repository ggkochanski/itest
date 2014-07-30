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
package org.itest.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.itest.impl.ITestParamMergerImpl;
import org.itest.json.simple.ITestSimpleJsonParamParserImpl;
import org.itest.param.ITestParamMerger;
import org.itest.param.ITestParamState;
import org.junit.Assert;
import org.junit.Test;

public class ITestParamsMergeProviderTest {

    @Test
    public void testSingle() {
        ITestParamMerger p = new ITestParamMergerImpl();
        ITestParamState state = new ITestSimpleJsonParamParserImpl().parse("'A':[{'a':[{'field':'vvv'}]}]");
        ITestParamState unified = p.merge(Collections.singleton("T=A:0"), Collections.singleton(state));
        Assert.assertEquals("vvv", unified.getElement("T").getElement("a").getElement("0").getElement("field").getValue());
    }

    @Test
    public void test() {
        ITestParamMerger p = new ITestParamMergerImpl();
        Collection<ITestParamState> states = new ArrayList<ITestParamState>();
        states.add(new ITestSimpleJsonParamParserImpl().parse("'T':{'a':[{'field':'vvv'}]}"));
        states.add(new ITestSimpleJsonParamParserImpl().parse("'A':[{'a':[{'field2':'vvv2'}]}]"));
        ITestParamState unified = p.merge(Arrays.asList("", "T=A:0"), states);
        Assert.assertEquals("vvv", unified.getElement("T").getElement("a").getElement("0").getElement("field").getValue());
        Assert.assertEquals("vvv2", unified.getElement("T").getElement("a").getElement("0").getElement("field2").getValue());
    }

}
