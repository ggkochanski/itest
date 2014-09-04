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
package org.itest.impl;

import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;
import org.itest.exception.ITestInitializationException;
import org.itest.param.ITestValueConverter;

public class ITestValueConverterImpl implements ITestValueConverter {

    @Override
    public <T> T convert(Class<T> clazz, String value) {
        Object res;
        if ( null == value ) {
            res = null;
        } else if ( String.class == clazz || Object.class == clazz ) {
            res = StringEscapeUtils.unescapeJava(value);
        } else if ( Integer.class == clazz || int.class == clazz ) {
            res = Integer.valueOf(value);
        } else if ( Long.class == clazz || long.class == clazz ) {
            res = Long.valueOf(value);
        } else if ( Double.class == clazz || double.class == clazz ) {
            res = Double.valueOf(value);
        } else if ( Boolean.class == clazz || boolean.class == clazz ) {
            res = Boolean.valueOf(value);
        } else if ( Character.class == clazz || char.class == clazz ) {
            value = StringEscapeUtils.unescapeJava(value);
            if ( value.length() > 1 ) {
                throw new ITestInitializationException("Character expected, found (" + value + ") " + value.length() + " characters.", null);
            }
            res = value.charAt(0);
        } else if ( Date.class == clazz ) {
            res = new Date(Long.valueOf(value));
        } else if ( clazz.isEnum() ) {
            res = Enum.valueOf((Class<Enum>) clazz, value);
        } else if ( Class.class == clazz ) {
            try {
                res = Class.forName(value);
            } catch (ClassNotFoundException e) {
                throw new ITestInitializationException(value, e);
            }
        } else {
            throw new ITestInitializationException(clazz.getName() + "(" + value + ")", null);
        }
        return (T) res;
    }
}
