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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.itest.ITestConfig;
import org.itest.annotation.ITest;
import org.itest.annotation.ITestAssignment;
import org.itest.annotation.ITestRef;
import org.itest.annotation.ITests;
import org.itest.definition.ITestDefinition;
import org.itest.definition.ITestDefinitionFactory;
import org.itest.exception.ITestParamDefinitionException;
import org.itest.param.ITestParamAssignment;
import org.itest.param.ITestParamState;

public class ITestDefinitionFactoryImpl implements ITestDefinitionFactory {

    private final Map<ITestIdentifier, Collection<ITestDependency>> itestDependencyMap = new HashMap<ITestIdentifier, Collection<ITestDependency>>();

    private final Map<ITestIdentifier, ITestDeclaration> itestMap = new HashMap<ITestIdentifier, ITestDeclaration>();

    private final Map<ITestIdentifier, ITestDefinition> itestDefinitionMap = new HashMap<ITestIdentifier, ITestDefinition>();

    private final ITestConfig iTestConfig;

    private final byte[] buffer = new byte[1024];

    public ITestDefinitionFactoryImpl(ITestConfig iTestConfig) {
        this.iTestConfig = iTestConfig;
    }

    @Override
    public Collection<ITestDefinition> buildTestFlowDefinitions(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            buildDependencies(clazz);
        }

        for (ITestIdentifier itestIdentifier : itestMap.keySet()) {
            buildDefinition(itestIdentifier);
        }
        return itestDefinitionMap.values();
    }

    private void buildDefinition(ITestIdentifier itestIdentifier) {
        Collection<ITestDependency> children = itestDependencyMap.get(itestIdentifier);
        if ( null != children ) {
            for (ITestDependency child : itestDependencyMap.get(itestIdentifier)) {
                buildDefinition(child.itestIdentifier);
            }
            define(itestIdentifier);
        }
    }

    private void define(ITestIdentifier itestIdentifier) {
        if ( null == itestDefinitionMap.get(itestIdentifier) ) {
            ITestDeclaration itestDefinition = itestMap.get(itestIdentifier);
            // Collection<String> transformations = new ArrayList<String>();
            // Collection<ITestParamState> params = new ArrayList<ITestParamState>();
            Collection<ITestParamAssignment> iTestParamAssignments = new ArrayList<ITestParamAssignment>();
            for (ITestDependency child : itestDependencyMap.get(itestIdentifier)) {
                ITestDefinition childPathDefintion = itestDefinitionMap.get(child.itestIdentifier);
                // transformations.add(child.transformation);
                ITestParamState childParams;
                if ( null == childPathDefintion ) {
                    childParams = loadParams(child.itestIdentifier);
                } else {
                    childParams = childPathDefintion.getInitParams();
                }
                // params.add(childParams);
                iTestParamAssignments.add(new ITestParamAssignmentImpl(child.transformation, childParams));
            }
            if ( 0 < itestDefinition.path.init().length() ) {
                iTestParamAssignments.add(new ITestParamAssignmentImpl("", parseInitParam(itestDefinition.method, itestDefinition.path.init())));
            }
            Map<Class<?>, Map<String, String>> iTestStaticAssignment = toITestStaticAssignment(itestDefinition.path.assignment());
            ITestParamAssignment[] iTestParamAssignmentsArray = iTestParamAssignments.toArray(new ITestParamAssignment[iTestParamAssignments.size()]);
            ITestParamState itestParams = iTestConfig.getITestParamsMerger().merge(iTestParamAssignmentsArray);
            ITestDefinition res = new ITestDefinitionImpl(itestDefinition.method.getDeclaringClass(), itestDefinition.method, itestIdentifier.itestName,
                    itestParams, parseInitParam(itestDefinition.method, itestDefinition.path.verify()), new HashMap<String, Type>(), iTestStaticAssignment);
            itestDefinitionMap.put(itestIdentifier, res);
        }
    }

    private ITestParamState loadParams(ITestIdentifier itestIdentifier) {
        return iTestConfig.getITestParamLoader().loadITestParam(itestIdentifier.itestClass, itestIdentifier.itestName);
    }

    private Map<Class<?>, Map<String, String>> toITestStaticAssignment(ITestAssignment[] assignment) {
        Map<Class<?>, Map<String, String>> assignmentMap = new HashMap<Class<?>, Map<String, String>>();
        for (ITestAssignment iTestAssignment : assignment) {
            Map<String, String> fieldMap = assignmentMap.get(iTestAssignment.targetClass());
            if ( null == fieldMap ) {
                fieldMap = new HashMap<String, String>();
                assignmentMap.put(iTestAssignment.targetClass(), fieldMap);
            }
            fieldMap.put(iTestAssignment.targetField(), iTestAssignment.sourcePath());
        }
        return assignmentMap;
    }

    private void buildDependencies(Class<?> clazz) {
        L: for (Method method : clazz.getDeclaredMethods()) {
            if ( method.isAnnotationPresent(ITests.class) ) {
                int methodTestCounter = 0;
                for (ITest path : method.getAnnotation(ITests.class).value()) {
                    String testName = path.name();
                    if ( 0 == testName.length() ) {
                        testName = method.getName() + "#itest" + methodTestCounter;
                    }
                    ITestIdentifier itestIdentifier = new ITestIdentifier(clazz, testName);
                    if ( null == itestMap.get(itestIdentifier) ) {
                        itestMap.put(itestIdentifier, new ITestDeclaration(method, path));
                        Collection<ITestDependency> col = new ArrayList<ITestDependency>();
                        itestDependencyMap.put(itestIdentifier, col);
                        for (ITestRef initRef : path.initRef()) {
                            Class<?> refClass = initRef.useClass() == ITestRef.class ? clazz : initRef.useClass();
                            String refTestName = initRef.use();
                            col.add(new ITestDependency(initRef.assign(), new ITestIdentifier(refClass, refTestName)));
                            if ( refClass != clazz ) {
                                buildDependencies(refClass);
                            }
                        }
                    } else {
                        break L;
                    }
                    methodTestCounter++;
                }
            }
        }
    }

    static class ITestDependency {
        private final String transformation;

        private final ITestIdentifier itestIdentifier;

        public ITestDependency(String transform, ITestIdentifier itestIdentifier) {
            this.transformation = transform;
            this.itestIdentifier = itestIdentifier;
        }
    }

    static class ITestIdentifier {
        Class<?> itestClass;

        String itestName;

        public ITestIdentifier(Class<?> itestClass, String itestName) {
            this.itestClass = itestClass;
            this.itestName = itestName;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((itestClass == null) ? 0 : itestClass.hashCode());
            result = prime * result + ((itestName == null) ? 0 : itestName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if ( this == obj ) {
                return true;
            }
            if ( obj == null ) {
                return false;
            }
            if ( getClass() != obj.getClass() ) {
                return false;
            }
            ITestIdentifier other = (ITestIdentifier) obj;
            if ( itestClass == null ) {
                if ( other.itestClass != null ) {
                    return false;
                }
            } else if ( !itestClass.equals(other.itestClass) ) {
                return false;
            }
            if ( itestName == null ) {
                if ( other.itestName != null ) {
                    return false;
                }
            } else if ( !itestName.equals(other.itestName) ) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return itestClass.getName() + "." + itestName;

        }
    }

    static class ITestDeclaration {
        private final Method method;

        private final ITest path;

        public ITestDeclaration(Method method, ITest path) {
            this.method = method;
            this.path = path;
        }

        public Method getMethod() {
            return method;
        }

        public ITest getPath() {
            return path;
        }
    }

    private ITestParamState parseInitParam(Method method, String init) {
        try {
            ITestParamState res = iTestConfig.getITestParamParser().parse(init);
            return res;
        } catch (RuntimeException e) {
            throw new ITestParamDefinitionException(method, null, init, e);
        }
    }
}