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

import com.google.common.reflect.TypeToken;
import org.apache.commons.lang.RandomStringUtils;
import org.itest.ITestConfig;
import org.itest.ITestConstants;
import org.itest.ITestContext;
import org.itest.ITestSuperObject;
import org.itest.annotation.ITestFieldAssignment;
import org.itest.annotation.ITestFieldClass;
import org.itest.exception.ITestException;
import org.itest.exception.ITestIllegalArgumentException;
import org.itest.exception.ITestInitializationException;
import org.itest.exception.ITestMethodExecutionException;
import org.itest.exception.ITestPossibleCycleException;
import org.itest.generator.ITestObjectGenerator;
import org.itest.impl.util.ITestUtils;
import org.itest.param.ITestParamState;
import org.itest.util.reflection.ITestFieldProvider;
import org.itest.util.reflection.ITestFieldProvider.FieldHolder;
import org.itest.util.reflection.ITestTypeTokenProvider;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class ITestRandomObjectGeneratorImpl implements ITestObjectGenerator {
    private static final int MAX_DEPTH = 20;

    private static final Class<?> PROXY_CLASS = Proxy.class;

    public static final ITestParamState EMPTY_STATE = new ITestParamStateImpl() {
        {
            elements = Collections.EMPTY_MAP;
        }
    };

    private static final Random random = new Random();

    private static int RANDOM_MAX = 5;

    private static int RANDOM_MIN = 2;

    private final ITestConfig iTestConfig;

    protected ITestTypeTokenProvider typeTokenProvider = new ITestTypeTokenProvider();
    protected ITestFieldProvider fieldProvider = new ITestFieldProvider(typeTokenProvider);

    public ITestRandomObjectGeneratorImpl(ITestConfig iTestConfig) {
        this.iTestConfig = iTestConfig;
    }

    @Override
    public Object generate(Type type, ITestParamState initParam, ITestContext iTestContext) {
        try {
            return generateRandom(type, iTestContext);
        } catch (ITestException e) {
            e.addPrefix(type.toString());
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T generateRandom(Class<T> clazz, ITestContext iTestContext) {
        Object res;
        ITestParamState iTestState = iTestContext.getCurrentParam();
        if (null != iTestState && null == iTestState.getNames()) {
            res = iTestConfig.getITestValueConverter().convert(clazz, iTestState.getValue());
        } else if (Void.class == clazz || void.class == clazz) {
            res = null;
        } else if (String.class == clazz && (null == iTestState || null == iTestState.getNames())) {
            res = RandomStringUtils.randomAlphanumeric(20);
        } else if (Long.class == clazz || long.class == clazz) {
            res = new Long(random.nextLong());
        } else if (Integer.class == clazz || int.class == clazz) {
            res = new Integer(random.nextInt());
        } else if (Boolean.class == clazz || boolean.class == clazz) {
            res = random.nextBoolean() ? Boolean.TRUE : Boolean.FALSE;
        } else if (Date.class == clazz) {
            res = new Date(random.nextLong());
        } else if (Double.class == clazz || double.class == clazz) {
            res = random.nextDouble();
        } else if (Float.class == clazz || float.class == clazz) {
            res = random.nextFloat();
        } else if (Character.class == clazz || char.class == clazz) {
            res = RandomStringUtils.random(1).charAt(0);
        } else if (clazz.isEnum()) {
            res = clazz.getEnumConstants()[random.nextInt(clazz.getEnumConstants().length)];
        } else if (clazz.isArray()) {
            int size = random.nextInt(RANDOM_MAX - RANDOM_MIN) + RANDOM_MIN;
            if (null != iTestState && iTestState.getSizeParam() != null) {
                size = iTestState.getSizeParam();
            }
            Object array = Array.newInstance(clazz.getComponentType(), size);
            for (int i = 0; i < size; i++) {
                iTestContext.enter(array, String.valueOf(i));
                ITestParamState elementITestState = iTestState == null ? null : iTestState.getElement(String.valueOf(i));
                Object value = generateRandom((Type) clazz.getComponentType(), iTestContext);
                Array.set(array, i, value);
                iTestContext.leave(value);
            }
            res = array;
        } else {
            res = newInstance(clazz, iTestContext);
            fillFields(clazz, res, iTestContext);
        }
        return (T) res;
    }

    protected Object newInstance(Class clazz, ITestContext iTestContext, Type... typeActualArguments) {
        Object res;
        Constructor<?> c = getConstructor(clazz);
        Class[] constructorTypes = c.getParameterTypes();
        Object[] constructorArgs = new Object[constructorTypes.length];
        for (int i = 0; i < constructorTypes.length; i++) {
            //TypeToken argType = typeToken.resolveType(constructorTypes[i]);
            iTestContext.enter(iTestContext.getCurrentOwner(), "<init[" + i + "]>");
            iTestContext.setEmptyParam();
            constructorArgs[i] = generateRandom((Type) constructorTypes[i], iTestContext);
            iTestContext.leave(constructorArgs[i]);
        }
        try {
            res = c.newInstance(constructorArgs);
        } catch (Exception e) {
            // Object[] args = new Object[constructorArgs.length + 1];
            // args[0] = generateRandom(clazz.getEnclosingClass(), null, null, owner, postProcess);
            // System.arraycopy(constructorArgs, 0, args, 1, constructorArgs.length);
            // try {
            // res = c.newInstance(args);
            // } catch (Exception ee) {
            // throw new RuntimeException(ee);
            // }
            throw new RuntimeException(e);
        }
        return res;
    }

    public <T> T generateRandom(Type type, ITestContext iTestContext) {
        ITestParamState iTestState = iTestContext.getCurrentParam();
        TypeToken typeToken = typeTokenProvider.getTypeToken(type);
        Class<?> clazz = typeToken.getRawType();

        Class<?> requestedClass = getClassFromParam(iTestState);
        if ( null != iTestState && null != iTestState.getAttribute(ITestConstants.ATTRIBUTE_DEFINITION) ) {
            ITestParamState iTestStateLoaded = iTestConfig.getITestParamLoader()
                    .loadITestParam(null == requestedClass ? clazz : requestedClass, iTestState.getAttribute(ITestConstants.ATTRIBUTE_DEFINITION))
                    .getElement(ITestConstants.THIS);
            iTestState = iTestConfig.getITestParamsMerger().merge(new ITestParamAssignmentImpl("", iTestStateLoaded), new ITestParamAssignmentImpl("", iTestState));
            iTestContext.replaceCurrentState(iTestState);
        }

        Object res;
        if (ITestParamState.class == clazz) {
            res = processITestState(iTestContext);
        } else if ( null != iTestState && null == iTestState.getSizeParam() && null == iTestState.getValue() ) {
            res = null;
        } else if ( null != iTestState && null != iTestState.getAttribute(ITestConstants.REFERENCE_ATTRIBUTE) ) {
            res = iTestContext.findGeneratedObject(iTestState.getAttribute(ITestConstants.REFERENCE_ATTRIBUTE));
        } else if ( PROXY_CLASS == requestedClass ) {
            res = newDynamicProxy(typeToken, iTestContext);
        } else if ( Collection.class.isAssignableFrom(clazz) ) {
            res = fillCollection(null, typeToken, iTestContext);
        } else if ( Map.class.isAssignableFrom(clazz) ) {
            res = fillMap(null, typeToken, iTestContext);
        } else if (null != requestedClass) {
            res = generateRandom(requestedClass, iTestContext);
        } else if (typeToken.getType() instanceof GenericArrayType) {
            int size = random.nextInt(RANDOM_MAX - RANDOM_MIN) + RANDOM_MIN;
            if (null != iTestState && iTestState.getSizeParam() != null) {
                size = iTestState.getSizeParam();
            }
            TypeToken componentType = typeToken.getComponentType();
            Object array = Array.newInstance(componentType.getRawType(), size);
            for (int i = 0; i < size; i++) {
                ITestParamState elementITestState = iTestState == null ? null : iTestState.getElement(String.valueOf(i));
                Array.set(array, i, generateRandom(componentType.getType(), iTestContext));
            }
            res = array;
        } else if (null != iTestState && null == iTestState.getNames()) {
            res = iTestConfig.getITestValueConverter().convert(clazz, iTestState.getValue());
        } else if (clazz.isInterface()) {
            res = newDynamicProxy(typeToken, iTestContext);
        } else if (typeToken.getType() instanceof Class) {
            res = generateRandom(clazz, iTestContext);
        } else if (Class.class == clazz) {
            res = generateRandom(clazz, iTestContext);
        } else if (Enum.class == clazz) {
            Type enumType = typeToken.resolveType(clazz.getTypeParameters()[0]).getType();
            res = generateRandom(enumType, iTestContext);
        } else {
            res = newInstance(clazz, iTestContext);
            fillFields(type, res, iTestContext);
        }
        return (T) res;
    }

    private ITestParamState processITestState(ITestContext iTestContext) {
        ITestParamState state = iTestContext.getCurrentParam();
        ITestParamStateImpl res = null;
        if (null != state) {
            res = new ITestParamStateImpl();
            if (null != state.getAttribute(ITestConstants.REFERENCE_ATTRIBUTE)) {
                ITestParamState ref = iTestContext.findGeneratedState(state.getAttribute(ITestConstants.REFERENCE_ATTRIBUTE));
                if(null == ref.getNames()){
                    res.setValue(ref.getValue());
                    state=res;
                }else {
                    state = iTestConfig.getITestParamsMerger().merge(new ITestParamAssignmentImpl("", ref), new ITestParamAssignmentImpl("", state));
                    iTestContext.replaceCurrentState(state);
                }
            }
            Iterable<String> attributes = state.getAttributeNames();
            if (null != attributes) {
                for (String attribute : attributes) {
                    res.addAttribute(attribute, state.getAttribute(attribute));
                }
            }
            Collection<String> names = state.getNames();
            if (null != names) {
                res.initElements();
                for (String name : names) {
                    iTestContext.enter(res, name);
                    ITestParamState element = processITestState(iTestContext);
                    res.addElement(name, element);
                    iTestContext.leave(element);
                }
            } else {
                res.setValue(state.getValue());
            }
        }
        return res;
    }

    private Class<?> getClassFromParam(ITestParamState iTestState) {
        Class<?> clazz = null;
        if (null != iTestState) {
            String reqClass = iTestState.getAttribute(ITestConstants.ATTRIBUTE_CLASS);
            if (null == reqClass && null != iTestState.getElement(ITestConstants.ATTRIBUTE_CLASS)) {
                reqClass = iTestState.getElement(ITestConstants.ATTRIBUTE_CLASS).getValue();
            }
            if (null != reqClass) {
                if (ITestConstants.DYNAMIC.equals(reqClass)) {
                    clazz = PROXY_CLASS;
                } else {
                    clazz = iTestConfig.getITestValueConverter().convert(Class.class, reqClass);
                }
            }
        }
        return clazz;
    }


    protected Object newDynamicProxy(TypeToken typeToken, final ITestContext iTestContext) {
        final Class<?> clazz = typeToken.getRawType();
        final Map<String, Object> methodResults = new HashMap<String, Object>();

        Object res = Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String mSignature = ITestUtils.getMethodSingnature(method, true);
                if (methodResults.containsKey(mSignature)) {
                    return methodResults.get(mSignature);
                }
                throw new ITestMethodExecutionException("Implementation of " + clazz.getName() + "." + mSignature + " not provided", null);
            }
        });


        TypeToken t = typeToken;
        do {
            for (Method m : t.getRawType().getDeclaredMethods()) {
                if (!Modifier.isStatic(m.getModifiers())) {
                    TypeToken mType = typeToken.resolveType(m.getGenericReturnType());
                    String signature = ITestUtils.getMethodSingnature(m, true);
                    ITestParamState iTestState = iTestContext.getCurrentParam();
                    ITestParamState mITestState = iTestState == null ? null : iTestState.getElement(signature);
                    if (null == mITestState) {
                        signature = ITestUtils.getMethodSingnature(m, false);
                    }
                    fillMethod(mType.getType(), m, res, signature, iTestContext, methodResults);
                }
            }
        }
        while (t.getRawType().getSuperclass() != null && (t = t.getSupertype(t.getRawType().getSuperclass())) != null);

        return res;
    }

    protected void fillMethod(Type mType, Method m, Object res, String mSignature, ITestContext iTestContext, Map<String, Object> methodResults) {
        try {
            iTestContext.enter(res, mSignature);
            ITestParamState mITestState = iTestContext.getCurrentParam();
            Object o = generateRandom(mType, iTestContext);
            methodResults.put(ITestUtils.getMethodSingnature(m, true), o);
            iTestContext.leave(o);
        } catch (ITestException e) {
            e.addPrefix(mSignature);
            throw e;
        } catch (IllegalArgumentException e) {
            throw new ITestIllegalArgumentException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    protected void fillFields(Type type, Object o, ITestContext iTestContext) {
        if (iTestContext.depth() > MAX_DEPTH) {
            throw new ITestPossibleCycleException("Possible cycle detected.");
        }
        ITestParamState itestState = iTestContext.getCurrentParam();
        if ( null != itestState && null != itestState.getNames() && o instanceof ITestSuperObject ) {
            ITestSuperObject iTestSuperObject = (ITestSuperObject) o;
            TypeToken typeToken = typeTokenProvider.getTypeToken(type);
            TypeToken contentType = resolveParametrizedType(typeToken, ITestSuperObject.class, 0);
            for (String name : itestState.getNames()) {
                iTestContext.enter(o, name);
                Object value = generate(contentType.getType(), null, iTestContext);
                iTestSuperObject.setField(name, value);
                iTestContext.leave(value);
            }
        } else {
            Collection<FieldHolder> fieldHolders = collectFields(type, iTestContext);
            for (FieldHolder f : fieldHolders) {
                fillField(f.getFieldType(), f.getField(), o, iTestContext);
            }
        }
    }

    protected Collection<FieldHolder> collectFields(Type type, ITestContext iTestContext) {
        return fieldProvider.collectFields(type);
    }

    protected void fillField(Type fType, Field f, Object o, ITestContext iTestContext) {
        f.setAccessible(true);
        try {
            iTestContext.enter(o, f.getName());
            ITestParamState fITestState = iTestContext.getCurrentParam();
            String fITestValue = fITestState == null ? null : fITestState.getValue();
            Object oRes;
            // if ( fITestState != null && null != fITestState.getAttribute(ITestConstants.REFERENCE_ATTRIBUTE) ) {
            // oRes = iTestContext.findGeneratedObject(fITestState.getAttribute(ITestConstants.REFERENCE_ATTRIBUTE));
            // } else
            if (null == fITestState && iTestContext.isStaticAssignmentRegistered(o.getClass(), f.getName())) {
                iTestContext.registerAssignment(o.getClass(), f.getName());
                oRes = null;//TODO: implement it
            } else if (null == fITestState && f.isAnnotationPresent(ITestFieldAssignment.class)) {
                iTestContext.registerAssignment(f.getAnnotation(ITestFieldAssignment.class).value());
                oRes = null;//TODO: implement it
            } else if (null == fITestState && f.isAnnotationPresent(ITestFieldClass.class)) {
                iTestContext.setEmptyParam();
                oRes = generateRandom(f.getAnnotation(ITestFieldClass.class).value(), iTestContext);
                f.set(o, oRes);
//            } else if (null != fITestValue) {
//                if (fITestValue.startsWith(":")) {
//                    // TODO: register assignment
//                    oRes = null;//TODO: implement it
//                } else {
//                    oRes = iTestConfig.getITestValueConverter().convert(f.getType(), fITestValue);
//                    f.set(o, oRes);
//                }
            } else {
                oRes = generateRandom(fType, iTestContext);
                f.set(o, oRes);
            }
            f.set(o, oRes);
            iTestContext.leave(oRes);
        } catch (ITestException e) {
            e.addPrefix(f.getName());
            throw e;
        } catch (IllegalArgumentException e) {
            throw new ITestIllegalArgumentException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    protected Object fillMap(Object o, TypeToken typeToken, ITestContext iTestContext) {
        ITestParamState iTestState = iTestContext.getCurrentParam();
        Map<Object, Object> m = (Map<Object, Object>) o;
        if(null!=iTestContext.getCurrentParam() && null!=iTestContext.getCurrentParam().getAttribute(ITestConstants.ATTRIBUTE_CLASS)){
            Class<?> mapClass = iTestConfig.getITestValueConverter().convert(Class.class,
                    iTestContext.getCurrentParam().getAttribute(ITestConstants.ATTRIBUTE_CLASS));
            m = (Map<Object, Object>) newInstance(mapClass, iTestContext);
        }
        if (null == m) {
            m = new HashMap<Object, Object>();
        } else {
            m.clear();
        }
        int size = random.nextInt(RANDOM_MAX - RANDOM_MIN) + RANDOM_MIN;
        if (null != iTestState && iTestState.getSizeParam() != null) {
            size = iTestState.getSizeParam();
            //to overwrite expected value
            if (0 == size) {
                iTestContext.enter(m, "<map>");
                iTestContext.leave(null);
            }
        }
        TypeToken keyType = resolveParametrizedType(typeToken, Map.class, 0);
        TypeToken valueType = resolveParametrizedType(typeToken, Map.class, 1);
        for (int i = 0; i < size; i++) {
            iTestContext.enter(m, String.valueOf(i));
            iTestContext.enter("Map.Entry", "key");
            ITestParamState eITestState = iTestState == null ? null : iTestState.getElement(String.valueOf(i));
            Object key = generateRandom(keyType.getType(), iTestContext);
            iTestContext.leave(key);
            iTestContext.enter("Map.Entry", "value");
            Object value = generateRandom(valueType.getType(), iTestContext);
            iTestContext.leave(value);
            m.put(key, value);
            iTestContext.leave("Map.Entry");
        }
        return m;
    }

    protected Object fillCollection(Object o, TypeToken typeToken, ITestContext iTestContext) {
        ITestParamState iTestState = iTestContext.getCurrentParam();
        Collection<Object> col = (Collection<Object>) o;
        Class collectionClass;
        if ( null != iTestContext.getCurrentParam() && null != iTestContext.getCurrentParam().getAttribute(ITestConstants.ATTRIBUTE_CLASS) ) {
            collectionClass = iTestConfig.getITestValueConverter().convert(Class.class,
                    iTestContext.getCurrentParam().getAttribute(ITestConstants.ATTRIBUTE_CLASS));
        } else {
            collectionClass = typeToken.getRawType();
        }
        if ( !collectionClass.isInterface() ) {
            col = (Collection<Object>) newInstance(collectionClass, iTestContext);
        } else {
            if ( Set.class.isAssignableFrom(collectionClass) ) {
                col = (Collection<Object>) newInstance(HashSet.class, iTestContext);
            } else {
                col = (Collection<Object>) newInstance(ArrayList.class, iTestContext);
            }
        }
        if ( null == col ) {
        } else {
            col.clear();
        }
        int size = random.nextInt(RANDOM_MAX - RANDOM_MIN) + RANDOM_MIN;
        if (null != iTestState && iTestState.getSizeParam() != null) {
            size = iTestState.getSizeParam();
            //to overwrite expected value
            if (0 == size) {
                iTestContext.enter(col, "<col>");
                iTestContext.leave(null);
            }
        }

        TypeToken elementType = resolveParametrizedType(typeToken, Collection.class, 0);
        for (int i = 0; i < size; i++) {
            iTestContext.enter(col, String.valueOf(i));
            Object value;
            value = generateRandom(elementType.getType(), iTestContext);
            col.add(value);
            iTestContext.leave(value);
        }
        return col;
    }

    private static Constructor<?> getConstructor(Class<?> clazz) {
        Constructor<?> res;
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (constructors.length == 0) {
            throw new ITestInitializationException(clazz, null);
        }
        res = constructors[0];
        for (int i = 1; i < constructors.length; i++) {
            if (constructors[i].getParameterTypes().length == 0) {
                res = constructors[i];
                break;
            }
        }
        try {
            res.setAccessible(true);
        } catch (SecurityException e) {
            throw new ITestInitializationException(clazz, e);
        }
        return res;
    }


    private static void log(String log) {
        // System.out.println(log);
    }

    public interface Builder {
        boolean ignore(Class<?> clazz);

        Object build(Type type);

        Object build(Class<?> clazz);
    }

    protected TypeToken resolveParametrizedType(TypeToken typeToken, Class clazz, int param) {
        TypeToken res;
        if (clazz.isAssignableFrom(typeToken.getRawType())) {
            res = typeToken.getSupertype(clazz).resolveType(clazz.getTypeParameters()[param]);
        } else {
            res = typeTokenProvider.getTypeToken(Object.class);
        }
        return res;
    }

}
