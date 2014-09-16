package org.itest.impl.declaration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.itest.declaration.ITest;
import org.itest.declaration.ITestDeclarationProvider;
import org.itest.declaration.ITests;

public class ITestDeclarationProviderCompositeImpl implements ITestDeclarationProvider {

    private final ITestDeclarationProvider[] providers;

    public ITestDeclarationProviderCompositeImpl(ITestDeclarationProvider... providers) {
        this.providers = providers;
    }

    @Override
    public ITests getITestDeclaration(Method m) {
        Collection<ITest> iTestCol = new ArrayList<ITest>();
        for (ITestDeclarationProvider provider : providers) {
            ITests iTests = provider.getITestDeclaration(m);
            if ( null != iTests ) {
                for (ITest iTest : iTests.value()) {
                    iTestCol.add(iTest);
                }
            }
        }
        ITests res = null;
        if ( iTestCol.size() > 0 ) {
            res = new ITestsInternal(iTestCol.toArray(new ITest[iTestCol.size()]));
        }
        return res;
    }

    static class ITestsInternal implements ITests {
        private final ITest[] value;

        public ITestsInternal(ITest[] value) {
            this.value = value;
        }

        @Override
        public ITest[] value() {
            return value;
        }

    }
}
