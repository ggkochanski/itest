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

import org.itest.param.ITestParamParser;
import org.itest.param.ITestParamState;

import com.eclipsesource.json.JsonObject;

public class ITestEclipseJsonParamParserImpl implements ITestParamParser {

    @Override
    public ITestParamState parse(String s) {
        s = prepare(s);
        JsonObject jsonObject = JsonObject.readFrom(s);

        return new ITestEclipseJsonState(jsonObject);
    }

    private String prepare(String s) {
        StringBuilder sb = new StringBuilder(s);
        if ( '{' != sb.charAt(0) ) {
            sb.insert(0, '{');
            sb.append('}');
        }
        for (int i = 0; i < sb.length(); i++) {
            if ( '\'' == sb.charAt(i) ) {
                if ( 0 < i && '\\' == sb.charAt(i - 1) ) {
                    sb.delete(i - 1, i);
                    i--;
                } else {
                    sb.setCharAt(i, '"');
                }
            }
        }
        // s = StringUtils.replace(StringUtils.replaceChars(s.toString(), '\'', '"'), "\\\"", "'");
        return sb.toString();
    }
}
