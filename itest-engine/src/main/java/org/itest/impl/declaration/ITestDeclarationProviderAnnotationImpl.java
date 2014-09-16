package org.itest.impl.declaration;

import java.lang.reflect.Method;

import org.itest.ITestConfig;
import org.itest.declaration.ITest;
import org.itest.declaration.ITestDeclarationProvider;
import org.itest.declaration.ITestRef;
import org.itest.declaration.ITests;
import org.itest.param.ITestParamState;

public class ITestDeclarationProviderAnnotationImpl implements ITestDeclarationProvider {

    private final ITestConfig iTestConfig;

    public ITestDeclarationProviderAnnotationImpl(ITestConfig iTestConfig) {
        this.iTestConfig = iTestConfig;
    }

    @Override
    public ITests getITestDeclaration(Method m) {
        ITests res = null;
        if ( m.isAnnotationPresent(org.itest.annotation.ITests.class) ) {
            res = new ITestsImpl(m.getAnnotation(org.itest.annotation.ITests.class));
        }
        return res;
    }

    class ITestsImpl implements ITests {

        private final ITest[] value;

        public ITestsImpl(org.itest.annotation.ITests annotation) {
            value = new ITest[annotation.value().length];
            for (int i = 0; i < value.length; i++) {
                value[i] = new ITestImpl(annotation.value()[i]);
            }
        }

        @Override
        public ITest[] value() {
            return value;
        }

    }

    class ITestImpl implements ITest {

        private final String name;

        private final ITestRef[] initRef;

        private final ITestParamState init;

        private final ITestParamState verify;

        public ITestImpl(org.itest.annotation.ITest iTest) {
            name = iTest.name();
            initRef = new ITestRef[iTest.initRef().length];
            for (int i = 0; i < initRef.length; i++) {
                initRef[i] = new ITestRefImpl(iTest.initRef()[i]);
            }
            init = iTestConfig.getITestParamParser().parse(iTest.init());
            verify = iTestConfig.getITestParamParser().parse(iTest.verify());
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public ITestRef[] initRef() {
            return initRef;
        }

        @Override
        public ITestParamState init() {
            return init;
        }

        @Override
        public ITestParamState verify() {
            return verify;
        }
    }

    static class ITestRefImpl implements ITestRef {

        private final Class<?> useClass;

        private final String use;

        private final String[] assign;

        public ITestRefImpl(org.itest.annotation.ITestRef iTestRef) {
            useClass = iTestRef.useClass() == org.itest.annotation.ITestRef.class ? null : iTestRef.useClass();
            use = iTestRef.use();
            assign = iTestRef.assign();
        }

        @Override
        public Class<?> useClass() {
            return useClass;
        }

        @Override
        public String use() {
            return use;
        }

        @Override
        public String[] assign() {
            return assign;
        }

    }
}
