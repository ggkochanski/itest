package org.itest.declaration;

import java.lang.reflect.Method;

public interface ITestDeclarationProvider {
    ITests getITestDeclaration(Method m);
}
