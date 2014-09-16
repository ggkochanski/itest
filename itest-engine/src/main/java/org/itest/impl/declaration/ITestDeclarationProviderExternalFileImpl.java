package org.itest.impl.declaration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.itest.ITestConfig;
import org.itest.ITestConstants;
import org.itest.declaration.ITest;
import org.itest.declaration.ITestDeclarationProvider;
import org.itest.declaration.ITestRef;
import org.itest.declaration.ITests;
import org.itest.exception.ITestDeclarationNotFoundException;
import org.itest.exception.ITestInitializationException;
import org.itest.impl.util.ITestUtils;
import org.itest.param.ITestParamState;

public class ITestDeclarationProviderExternalFileImpl implements ITestDeclarationProvider {

    public static final String INIT_REF = "initRef";

    private static final String INIT = "init";

    private static final String VERIFY = "verify";

    private static final String USE_CLASS = "useClass";

    private static final String USE = "use";

    private static final String ASSIGN = "assign";

    private static final String[] EMPTY_ASSIGNMENT = { "" };

    private static final ITestRef[] EMPTY_REF = new ITestRef[0];

    private final ITestConfig iTestConfig;

    public ITestDeclarationProviderExternalFileImpl(ITestConfig iTestConfig) {
        this.iTestConfig = iTestConfig;
    }

    @Override
    public ITests getITestDeclaration(Method m) {
        ITestParamState iTestParam = null;
        ITestParamState iTestParam2 = null;
        try {
            iTestParam = iTestConfig.getITestParamLoader().loadITestParam(m.getDeclaringClass(), ITestUtils.getMethodSingnature(m, true));
        } catch (ITestDeclarationNotFoundException e) {
        }
        try {
            iTestParam2 = iTestConfig.getITestParamLoader().loadITestParam(m.getDeclaringClass(), ITestUtils.getMethodSingnature(m, false));
        } catch (ITestDeclarationNotFoundException e) {
        }

        ITests res = null;
        if ( null != iTestParam || null != iTestParam2 ) {
            res = new ITestsImpl(iTestParam, iTestParam2);
        }
        return res;
    }

    static class ITestsImpl implements ITests {

        private final ITest[] value;

        public ITestsImpl(ITestParamState iTestParam, ITestParamState iTestParam2) {
            Collection<ITest> iTestCol = new ArrayList<ITest>();
            add(iTestParam, iTestCol);
            add(iTestParam2, iTestCol);
            value = iTestCol.toArray(new ITest[iTestCol.size()]);
        }

        private void add(ITestParamState iTestParam, Collection<ITest> iTestCol) {
            if ( null != iTestParam && null != iTestParam.getElement(ITestConstants.THIS) ) {
                for (String name : iTestParam.getElement(ITestConstants.THIS).getNames()) {
                    iTestCol.add(new ITestImpl(name, iTestParam.getElement(ITestConstants.THIS).getElement(name)));
                }
            }

        }

        @Override
        public ITest[] value() {
            return value;
        }

    }

    static class ITestImpl implements ITest {

        private final String name;

        private ITestRef[] initRef;

        private final ITestParamState init;

        private final ITestParamState verify;

        public ITestImpl(String name, ITestParamState element) {
            this.name = name;
            ITestParamState initRef = element.getElement(INIT_REF);
            if ( null != initRef ) {
                Collection<ITestRef> iTestRefCol = new ArrayList<ITestRef>();
                if ( null == initRef.getElement("0") ) {
                    this.initRef = new ITestRef[] { new ITestRefImpl(initRef) };
                } else {
                    // order matters
                    for (int i = 0;; i++) {
                        ITestParamState initRefParam = initRef.getElement(String.valueOf(i));
                        if ( null == initRefParam ) {
                            break;
                        }
                        iTestRefCol.add(new ITestRefImpl(initRefParam));
                    }
                    this.initRef = iTestRefCol.toArray(new ITestRef[iTestRefCol.size()]);
                }
            } else {
                this.initRef = EMPTY_REF;
            }
            this.init = element.getElement(INIT);
            this.verify = element.getElement(VERIFY);
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

        private String[] assign;

        public ITestRefImpl(ITestParamState initRefParam) {
            ITestParamState useClassParam = initRefParam.getElement(USE_CLASS);
            if ( null != useClassParam && null != useClassParam.getValue() ) {
                try {
                    this.useClass = Class.forName(useClassParam.getValue());
                } catch (ClassNotFoundException e) {
                    throw new ITestInitializationException(useClassParam.getValue(), e);
                }
            } else {
                useClass = null;
            }
            ITestParamState useParam = initRefParam.getElement(USE);
            if ( useParam != null ) {
                use = useParam.getValue();
            } else {
                use = null;
            }
            ITestParamState assignParam = initRefParam.getElement(ASSIGN);
            if ( null != assignParam ) {
                if ( null == assignParam.getElement("0") ) {
                    assign = new String[] { assignParam.getValue() };
                } else {
                    Collection<String> assignCol = new ArrayList<String>();
                    for (int i = 0;; i++) {
                        ITestParamState assignP = assignParam.getElement(String.valueOf(i));
                        if ( null != assignP ) {
                            break;
                        }
                        assignCol.add(assignP.getValue());
                    }
                    assign = assignCol.toArray(new String[assignCol.size()]);
                }
            } else {
                assign = EMPTY_ASSIGNMENT;
            }
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
