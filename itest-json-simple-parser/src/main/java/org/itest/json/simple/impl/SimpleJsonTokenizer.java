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

public class SimpleJsonTokenizer {

    private int index;

    private final String source;

    public SimpleJsonTokenizer(String s) {
        this.index = 0;
        this.source = s;
    }

    public void back() {
        if ( this.index > 0 ) {
            this.index -= 1;
        }
    }

    public boolean more() {
        return this.index < this.source.length();
    }

    public char next() {
        if ( more() ) {
            char c = source.charAt(this.index);
            this.index += 1;
            return c;
        }
        return 0;
    }

    public char nextClean() throws SimpleJsonException {
        for (;;) {
            char c = next();
            if ( c == '/' ) {
                switch (next()) {
                    case '/':
                        do {
                            c = next();
                        } while (c != '\n' && c != '\r' && c != 0);
                        break;
                    case '*':
                        for (;;) {
                            c = next();
                            if ( c == 0 ) {
                                throw syntaxError("Unclosed comment.");
                            }
                            if ( c == '*' ) {
                                if ( next() == '/' ) {
                                    break;
                                }
                                back();
                            }
                        }
                        break;
                    default:
                        back();
                        return '/';
                }
            } else if ( c == '#' ) {
                do {
                    c = next();
                } while (c != '\n' && c != '\r' && c != 0);
            } else if ( c == 0 || c > ' ' ) {
                return c;
            }
        }
    }

    public String stringTill(char quote) throws SimpleJsonException {
        StringBuilder sb = new StringBuilder();
        char c;
        while (true) {
            c = next();
            if ( 0 == c ) {
                throw syntaxError("Unterminated string");
            } else if ( quote == c ) {
                return sb.toString();
            } else {
                sb.append(c);
            }
        }
    }

    public String nextValue() throws SimpleJsonException {
        char c = nextClean();
        String s;

        switch (c) {
            case '"':
            case '\'':
                return stringTill(c);
            default:
                StringBuffer sb = new StringBuffer();
                while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
                    sb.append(c);
                    c = next();
                }
                back();

                s = sb.toString().trim();
                if ( s.equals("") ) {
                    throw syntaxError("Missing value.");
                }
                if ( s.toLowerCase().equals("null") ) {
                    return null;
                }
                return s;
        }

    }

    public SimpleJsonException syntaxError(String message) {
        return new SimpleJsonException(message + toString());
    }

    @Override
    public String toString() {
        return " at character " + this.index + " of " + this.source;
    }
}