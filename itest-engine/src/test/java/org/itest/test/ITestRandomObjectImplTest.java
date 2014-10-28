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

import org.itest.ITestContext;
import org.itest.config.ITestConfigImpl;
import org.itest.impl.ITestContextImpl;
import org.itest.impl.ITestRandomObjectGeneratorImpl;
import org.itest.json.simple.ITestSimpleJsonParamParserImpl;
import org.itest.param.ITestParamParser;
import org.itest.param.ITestParamState;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class ITestRandomObjectImplTest {
    @Test
    public void generateTest() {
        ITestRandomObjectGeneratorImpl g = new ITestRandomObjectGeneratorImpl(new ITestConfigImpl());
        ITestParamParser parser = new ITestSimpleJsonParamParserImpl();
        ITestParamState params = parser.parse("'arg':[{'name':'name1','classes':[{},{'name':'class1'},{},{}]}]");
        ITestParamState p=params.getElement("arg").getElement(String.valueOf(0));
        ITestContext ctx = new ITestContextImpl(p,Collections.EMPTY_MAP);
        Person person = (Person) g.generate(Person.class, p, null, ctx);
        Assert.assertEquals("name1", person.name);
        Assert.assertEquals(4, person.classes.length);
        Assert.assertEquals("class1", person.classes[1].name);

        parser.parse("'res':{'name':'name1'}");
    }

    static class Person {
        String name;

        int age;

        Classes[] classes;

    }

    static class Classes {
        String name;

        int rating;
    }
}
