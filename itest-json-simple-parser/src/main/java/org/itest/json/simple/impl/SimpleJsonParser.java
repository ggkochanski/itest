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
package org.itest.json.simple.impl;

import java.util.HashMap;
import java.util.Map;

public class SimpleJsonParser {
    public static SimpleJsonState readFrom(String in) {
        SimpleJsonTokenizer x = new SimpleJsonTokenizer(in);

        SimpleJsonState res = new SimpleJsonParser().read(x);
        return res;
    }

    private SimpleJsonState read(SimpleJsonTokenizer x) {
        SimpleJsonState res;
        char c = x.nextClean();
        x.back();
        switch (c) {
            case '{':
                res = readJsonObject(x);
                break;
            case '[':
                res = readJsonArray(x);
                break;
            default:
                res = readJsonValue(x);
        }
        return res;
    }

    private static SimpleJsonState readJsonValue(SimpleJsonTokenizer x) {
        String value = x.nextValue();
        return new SimpleJsonState(value);
    }

    private SimpleJsonState readJsonArray(SimpleJsonTokenizer x) {
        Map<String, SimpleJsonState> elements = new HashMap<String, SimpleJsonState>();
        SimpleJsonState res = new SimpleJsonState(elements);
        if ( x.nextClean() != '[' ) {
            throw x.syntaxError("A JSONArray text must start with '['");
        }
        char c;
        L: for (int i = 0;; i++) {
            c = x.nextClean();
            switch (c) {
                case ',':
                    elements.put(String.valueOf(i), null);
                    break;
                case ']':
                    break L;
                default:
                    x.back();
                    elements.put(String.valueOf(i), read(x));
                    c = x.nextClean();
                    if ( ']' == c ) {
                        break L;
                    } else if ( ',' == c ) {
                    } else {
                        throw x.syntaxError("Expected a ',' or ']'");
                    }
            }
        }
        return res;
    }

    private SimpleJsonState readJsonObject(SimpleJsonTokenizer x) {
        Map<String, SimpleJsonState> elements = new HashMap<String, SimpleJsonState>();
        SimpleJsonState res = new SimpleJsonState(elements);

        char c;
        String key;

        if ( x.nextClean() != '{' ) {
            throw x.syntaxError("A JSONObject text must begin with '{'");
        }
        L: for (;;) {
            c = x.nextClean();
            switch (c) {
                case 0:
                    throw x.syntaxError("A JSONObject text must end with '}'");
                case '}':
                    break L;
                default:
                    x.back();
                    key = x.nextValue();
            }

            c = x.nextClean();
            if ( c != ':' ) {
                throw x.syntaxError("Expected a ':' after a key");
            }
            elements.put(key, read(x));

            switch (x.nextClean()) {
                case ';':
                case ',':
                    if ( x.nextClean() == '}' ) {
                        break L;
                    }
                    x.back();
                    break;
                case '}':
                    break L;
                default:
                    throw x.syntaxError("Expected a ',' or '}'");
            }
        }
        return res;
    }
}
