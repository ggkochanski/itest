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
package org.itest.json.simple;

import org.itest.json.simple.impl.SimpleJsonState;
import org.itest.param.ITestParamState;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ITestSimpleJsonState implements ITestParamState {

    private String value;

    private Map<String, ITestSimpleJsonState> elements;

    private Map<String, String> attributes;

    public ITestSimpleJsonState(SimpleJsonState simpleJsonState) {
        Iterable<String> i = simpleJsonState.names();
        if ( null == i ) {
            this.value = simpleJsonState.getValue();
        } else {
            boolean valueSet=false;
            for (String key : i) {
                SimpleJsonState element = simpleJsonState.get(key);
                if ( key.startsWith("@") ) {
                    addAttribute(key.substring(1), element.getValue());
                } else if ( "_".equals(key) ) {
                    Iterable<String> names = element.names();
                    if ( null == names ) {
                        this.value = element.getValue();
                        valueSet = true;
                    } else {
                        if ( null == elements ) {
                            elements = new HashMap<String, ITestSimpleJsonState>();
                        }
                        for (String name : names) {
                            elements.put(name, new ITestSimpleJsonState(element.get(name)));
                        }
                    }
                } else {
                    ITestSimpleJsonState state = null;
                    if ( null != element ) {
                        state = new ITestSimpleJsonState(element);
                    }
                    if ( null == elements ) {
                        elements = new HashMap<String, ITestSimpleJsonState>();
                    }
                    elements.put(key, state);
                }
            }
            if(!valueSet&&null==elements){
                elements = new HashMap<String, ITestSimpleJsonState>();
            }
        }
    }

    private void addAttribute(String key, String value) {
        if ( null == attributes ) {
            attributes = new HashMap<String, String>();
        }
        attributes.put(key, value);
    }

    @Override
    public Integer getSizeParam() {
        return null == elements ? null : elements.size();
    }

    @Override
    public Iterable<String> getNames() {
        return null == elements ? null : elements.keySet();
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
    public String getAttribute(String name) {
        return null == attributes ? null : attributes.get(name);
    }

    public Iterable<String> getAttributeNames() {
        return null == attributes ? null : attributes.keySet();
    }

    private static Map<String, String> emptyMap() {
        return Collections.emptyMap();
    }

}
