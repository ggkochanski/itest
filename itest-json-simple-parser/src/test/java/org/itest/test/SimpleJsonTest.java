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

import org.apache.commons.lang.StringEscapeUtils;
import org.itest.json.simple.impl.SimpleJsonParser;
import org.itest.json.simple.impl.SimpleJsonState;
import org.junit.Assert;
import org.junit.Test;

public class SimpleJsonTest {
    @Test
    public void test() {
        SimpleJsonState o = SimpleJsonParser.readFrom("{a:a,b:b,t:[{},1,2,,4,null]}");
        Assert.assertEquals("a", o.get("a").getValue());
        Assert.assertEquals(null, o.get("t").get("3"));
        Assert.assertEquals("4", o.get("t").get("4").getValue());
        Assert.assertEquals(null, o.get("t").get("5").getValue());
    }

    private static final String[] escapeExamples = new String[]{
            "abc", "abc'", "abc\""
    };

    @Test
    public void escapeTest() {
        for (String example : escapeExamples) {
            String s = StringEscapeUtils.escapeJava(example);
            SimpleJsonState o2 = SimpleJsonParser.readFrom("{a:\"" + s + "\"}");
            Assert.assertEquals(example.length(), StringEscapeUtils.unescapeJava(o2.get("a").getValue()).length());

        }
    }
}
