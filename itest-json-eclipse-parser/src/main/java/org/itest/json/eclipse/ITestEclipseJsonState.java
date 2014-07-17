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
package org.itest.json.eclipse;

import java.util.LinkedHashMap;
import java.util.Map;

import org.itest.param.ITestParamState;

import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;

public class ITestEclipseJsonState implements ITestParamState {

    private String value;

    private Map<String, ITestEclipseJsonState> elements;

    public ITestEclipseJsonState(JsonValue value) {
        if ( value.isObject() ) {
            elements = new LinkedHashMap<String, ITestEclipseJsonState>();
            for (Member member : value.asObject()) {
                elements.put(member.getName(), new ITestEclipseJsonState(member.getValue()));
            }
        } else if ( value.isArray() ) {
            elements = new LinkedHashMap<String, ITestEclipseJsonState>();
            for (int i = 0; i < value.asArray().size(); i++) {
                elements.put(String.valueOf(i), new ITestEclipseJsonState(value.asArray().get(i)));
            }
        } else {
            this.value = value.asString();
        }
    }

    @Override
    public Integer getSizeParam() {
        return null == elements ? null : elements.size();
    }

    @Override
    public ITestParamState getElement(String name) {
        return null == elements ? null : elements.get(name);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public Iterable<String> getNames() {
        return elements == null ? null : elements.keySet();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if ( null == elements ) {
            sb.append(":").append(getValue());
        } else {
            sb.append(elements);
        }
        return sb.toString();
    }
}
