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
package org.itest;

import java.util.List;
import java.util.Map;

import org.itest.param.ITestParamState;

public interface ITestContext {

    void registerAssignment(String sourcePath);

    void registerAssignment(Class<?> clazz, String name);

    void enter(Object owner, String field);

    void leave(Object constructorArg);

    int depth();

    boolean isStaticAssignmentRegistered(Class<?> clazz, String name);

    Map<List<String>, List<String>> getAssignments();

    Object getCurrentOwner();

    String getCurrentField();

    ITestParamState getCurrentParam();

    void setEmptyParam();

    Object findGeneratedObject(String path);
}
