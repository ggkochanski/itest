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

import org.apache.commons.lang.RandomStringUtils;
import org.itest.ITestConfig;
import org.itest.ITestConstants;
import org.itest.ITestContext;
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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class ITestRandomObjectGeneratorImpl implements ITestObjectGenerator {
    private static final int MAX_DEPTH = 20;

    public static final ITestParamState EMPTY_STATE = new ITestParamStateImpl() {
        {
            elements = Collections.EMPTY_MAP;
        }
    };

    private static final Random random = new Random();

    private static int RANDOM_MAX = 5;

    private static int RANDOM_MIN = 2;

    private final ITestConfig iTestConfig;

    private Comparator<? super FieldHolder> fieldComparator = new Comparator<FieldHolder>() {
        @Override
        public int compare(FieldHolder o1, FieldHolder o2) {
            return o1.field.getName().compareTo(o2.field.getName());
        }
    };

    public ITestRandomObjectGeneratorImpl(ITestConfig iTestConfig) {
        this.iTestConfig = iTestConfig;
    }

    @Override
    public Object generate(Type type, ITestParamState initParam, Map<String, Type> itestGenericMap, ITestContext iTestContext) {
        try {
            return generateRandom(type, itestGenericMap, iTestContext);
        } catch (ITestException e) {
            e.addPrefix(type.toString());
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T generateRandom(Class<T> clazz, Map<String, Type> itestGenericMap, ITestContext iTestContext) {
        Object res;
        ITestParamState iTestState = iTestContext.getCurrentParam();
        if (null != iTestState && null == iTestState.getNames()) {
            res = iTestConfig.getITestValueConverter().convert(clazz, iTestState.getValue());
        } else if (Void.class == clazz || void.class == clazz) {
            res = null;
        } else if (String.class == clazz) {
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
                Object value = generateRandom(clazz.getComponentType(), itestGenericMap, iTestContext);
                Array.set(array, i, value);
                iTestContext.leave(value);
            }
            res = array;
        } else if (null != iTestState && null != iTestState.getElement("class")
                && !clazz.equals(iTestConfig.getITestValueConverter().convert(Class.class, iTestState.getElement("class").getValue()))) {
            res = generateRandom(iTestConfig.getITestValueConverter().convert(Class.class, iTestState.getElement("class").getValue()), itestGenericMap,
                    iTestContext);
        } else {
            res = newInstance(clazz, iTestContext);
            fillFields(clazz, res, iTestState, Collections.EMPTY_MAP, iTestContext);
        }
        return (T) res;
    }

    protected Object newInstance(Class<?> clazz, ITestContext iTestContext, Type... typeActualArguments) {
        Object res;
        Constructor<?> c = getConstructor(clazz);
        final Map<String, Type> map = new HashMap<String, Type>();
        TypeVariable<?> typeArguments[] = clazz.getTypeParameters();
        for (int i = 0; i < typeActualArguments.length; i++) {
            map.put(typeArguments[i].getName(), typeActualArguments[i]);
        }
        Type[] constructorTypes = c.getGenericParameterTypes();
        Object[] constructorArgs = new Object[constructorTypes.length];
        for (int i = 0; i < constructorTypes.length; i++) {
            Type pt = getTypeProxy(constructorTypes[i], map);
            iTestContext.enter(iTestContext.getCurrentOwner(), "<init[" + i + "]>");
            iTestContext.setEmptyParam();
            constructorArgs[i] = generateRandom(pt, Collections.EMPTY_MAP, iTestContext);
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

    public <T> T generateRandom(Type type, Map<String, Type> itestGenericMap, ITestContext iTestContext) {
        ITestParamState iTestState = iTestContext.getCurrentParam();
        Object res;
        if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;
            Class<?> componentClass;
            int size = random.nextInt(RANDOM_MAX - RANDOM_MIN) + RANDOM_MIN;
            if (null != iTestState && iTestState.getSizeParam() != null) {
                size = iTestState.getSizeParam();
            }
            Map<String, Type> map = new HashMap<String, Type>(itestGenericMap);
            if (arrayType.getGenericComponentType() instanceof ParameterizedType) {
                componentClass = (Class<?>) ((ParameterizedType) arrayType.getGenericComponentType()).getRawType();

            } else {
                componentClass = (Class<?>) arrayType.getGenericComponentType();
            }
            Object array = Array.newInstance(componentClass, size);
            for (int i = 0; i < size; i++) {
                ITestParamState elementITestState = iTestState == null ? null : iTestState.getElement(String.valueOf(i));
                Array.set(array, i, generateRandom(arrayType.getGenericComponentType(), map, iTestContext));
            }
            res = array;
        } else {
            Class<?> clazz;
            if (type instanceof Class<?>) {
                clazz = (Class<?>) type;
            } else if (type instanceof WildcardType) {
                clazz = inferClassTypeFromWildcardType(type);
            } else {
                clazz = (Class<?>) ((ParameterizedType) type).getRawType();
            }
            if (null != iTestState && null == iTestState.getNames()) {
                String path;
                if ( null != iTestState.getAttribute(ITestConstants.REFERENCE_ATTRIBUTE) ) {
                    res = iTestContext.findGeneratedObject(iTestState.getAttribute(ITestConstants.REFERENCE_ATTRIBUTE));
                } else {
                    res = iTestConfig.getITestValueConverter().convert(clazz, iTestState.getValue());
                }
            } else if (null != iTestState && null != iTestState.getElement("class")
                    && ITestConstants.DYNAMIC.equals(iTestState.getElement("class").getValue())) {
                res = newDynamicProxy(type, iTestState, itestGenericMap, iTestContext);
            } else if (null != iTestState && null != iTestState.getElement("class")) {
                clazz = iTestConfig.getITestValueConverter().convert(Class.class, iTestState.getElement("class").getValue());
                res = generateRandom(clazz, itestGenericMap, iTestContext);
            } else if (Collection.class.isAssignableFrom(clazz)) {
                res = fillCollection(getCurrentObject(iTestContext), type, itestGenericMap, iTestContext);
            } else if (Map.class.isAssignableFrom(clazz)) {
                res = fillMap(getCurrentObject(iTestContext), type, itestGenericMap, iTestContext);
            } else if (clazz.isInterface()) {
                res = newDynamicProxy(type, iTestState, itestGenericMap, iTestContext);
            } else if (type instanceof Class) {
                res = generateRandom(clazz, itestGenericMap, iTestContext);
            } else if (Enum.class == clazz) {
                Type enumType = getTypeProxy(((ParameterizedType) type).getActualTypeArguments()[0], itestGenericMap);
                res = generateRandom(enumType, itestGenericMap, iTestContext);
            } else if (Class.class == clazz) {
                // probably proxy will be required here
                res = ((ParameterizedType) type).getActualTypeArguments()[0];
            } else {
                Type[] typeActualArguments = {};
                if (!(type instanceof WildcardType)) {
                    typeActualArguments = ((ParameterizedType) type).getActualTypeArguments();
                }
                TypeVariable<?> typeArguments[] = clazz.getTypeParameters();
                final Map<String, Type> map = new HashMap<String, Type>(itestGenericMap);
                for (int i = 0; i < typeActualArguments.length; i++) {
                    map.put(typeArguments[i].getName(), typeActualArguments[i]);
                }

                res = newInstance(clazz, iTestContext, typeActualArguments);
                fillFields(clazz, res, iTestState, map, iTestContext);
            }
        }
        return (T) res;
    }

    private Class<?> inferClassTypeFromWildcardType(Type type) {
        Class<?> resultClass = null;
        if (type instanceof WildcardType) {
            WildcardType sampleType = (WildcardType) type;
            Type[] lowerBounds = sampleType.getLowerBounds();
            Type[] upperBounds = sampleType.getUpperBounds();
            if (lowerBounds.length != 0) {
                resultClass = (Class<?>) lowerBounds[0];
            } else if (upperBounds.length != 0) {
                resultClass = (Class<?>) upperBounds[0];
            }
        }
        return resultClass;
    }

    private Object getCurrentObject(ITestContext iTestContext) {
        try {
            Object owner = iTestContext.getCurrentOwner();
            String fName = iTestContext.getCurrentField();
            Class<?> clazz = owner.getClass();
            Object res = null;
            do {
                try {
                    Field f = clazz.getDeclaredField(fName);
                    f.setAccessible(true);
                    res = f.get(owner);
                } catch (NoSuchFieldException e) {
                }
            } while (null != (clazz = clazz.getSuperclass()));
            return res;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected Object newDynamicProxy(Type type, final ITestParamState iTestStateNotUsed, final Map<String, Type> itestGenericMap,
            final ITestContext iTestContext) {
        final Class<?> clazz = getRawClass(type);
        Map<String, Type> map = new HashMap<String, Type>();
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

        if (type instanceof ParameterizedType) {
            Type typeActualArguments[] = ((ParameterizedType) type).getActualTypeArguments();
            TypeVariable<?> typeArguments[] = clazz.getTypeParameters();
            for (int i = 0; i < typeActualArguments.length; i++) {
                map.put(typeArguments[i].getName(), typeActualArguments[i]);
            }
        }

        Class<?> t = clazz;
        do {
            for (Method m : t.getDeclaredMethods()) {
                if (!Modifier.isStatic(m.getModifiers())) {
                    String signature = ITestUtils.getMethodSingnature(m, true);
                    ITestParamState iTestState = iTestContext.getCurrentParam();
                    ITestParamState mITestState = iTestState == null ? null : iTestState.getElement(signature);
                    if (null == mITestState) {
                        mITestState = iTestState == null ? null : iTestState.getElement(signature = ITestUtils.getMethodSingnature(m, false));
                    }
                    fillMethod(m, res, signature, map, iTestContext, methodResults);
                }
            }
            if (clazz.getGenericSuperclass() instanceof ParameterizedType) {
                Type typeActualArguments[] = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();
                TypeVariable<?> typeArguments[] = clazz.getSuperclass().getTypeParameters();
                final Map<String, Type> map2 = new HashMap<String, Type>();
                for (int i = 0; i < typeActualArguments.length; i++) {
                    Type ta = map.get(typeActualArguments[i].toString());
                    map2.put(typeArguments[i].getName(), ta == null ? typeActualArguments[i] : ta);
                }
                map = map2;
            }
        } while ((t = t.getSuperclass()) != null);

        return res;
    }

    protected void fillMethod(Method m, Object res, String mSignature, Map<String, Type> map, ITestContext iTestContext, Map<String, Object> methodResults) {
        try {
            iTestContext.enter(res, mSignature);
            ITestParamState mITestState = iTestContext.getCurrentParam();
            Type rType = getTypeProxy(m.getGenericReturnType(), map);
            Object o = generateRandom(rType, map, iTestContext);
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

    protected void fillFields(Class<?> clazz, Object o, ITestParamState itestStateNotUsed, Map<String, Type> map, ITestContext iTestContext) {
        if (iTestContext.depth() > MAX_DEPTH) {
            throw new ITestPossibleCycleException("Possible cycle detected.");
        }
        Collection<FieldHolder> fieldHolders = collectFields(clazz, map);
        ITestParamState itestState = iTestContext.getCurrentParam();
        for (FieldHolder f : fieldHolders) {
            fillField(f.field, o, itestState == null ? null : itestState.getElement(f.field.getName()), f.map, iTestContext);
        }
    }

    protected Comparator<? super FieldHolder> getFieldComparator() {
        return fieldComparator;
    }

    private class FieldHolder {
        private Field field;

        private Map<String, Type> map;

        public FieldHolder(Field field, Map<String, Type> map) {
            this.field = field;
            this.map = map;
        }
    }

    private Collection<FieldHolder> collectFields(Class<?> clazz, Map<String, Type> map) {
        Collection<FieldHolder> res = new TreeSet<FieldHolder>(getFieldComparator());
        Class<?> t = clazz;
        do {
            for (Field f : t.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers()) && !"this$0".equals(f.getName())) {
                    res.add(new FieldHolder(f, map));
                }
            }
            if (clazz.getGenericSuperclass() instanceof ParameterizedType) {
                Type typeActualArguments[] = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();
                TypeVariable<?> typeArguments[] = clazz.getSuperclass().getTypeParameters();
                final Map<String, Type> map2 = new HashMap<String, Type>();
                for (int i = 0; i < typeActualArguments.length; i++) {
                    Type ta = map.get(typeActualArguments[i].toString());
                    map2.put(typeArguments[i].getName(), ta == null ? typeActualArguments[i] : ta);
                }
                map = map2;
            }
        } while ((t = t.getSuperclass()) != null);

        return res;
    }

    protected void fillField(Field f, Object o, ITestParamState fITestStateNotUsed, Map<String, Type> map, ITestContext iTestContext) {
        f.setAccessible(true);
        try {
            Type fType = getTypeProxy(f.getGenericType(), map);
            iTestContext.enter(o, f.getName());
            ITestParamState fITestState = iTestContext.getCurrentParam();
            String fITestValue = fITestState == null ? null : fITestState.getValue();
            Object oRes;
            // if ( fITestState != null && null != fITestState.getAttribute(ITestConstants.REFERENCE_ATTRIBUTE) ) {
            // oRes = iTestContext.findGeneratedObject(fITestState.getAttribute(ITestConstants.REFERENCE_ATTRIBUTE));
            // } else
            if ( null == fITestState && iTestContext.isStaticAssignmentRegistered(o.getClass(), f.getName()) ) {
                iTestContext.registerAssignment(o.getClass(), f.getName());
                oRes=null;//TODO: implement it
            } else if (null == fITestState && f.isAnnotationPresent(ITestFieldAssignment.class)) {
                iTestContext.registerAssignment(f.getAnnotation(ITestFieldAssignment.class).value());
                oRes=null;//TODO: implement it
            } else if (null == fITestState && f.isAnnotationPresent(ITestFieldClass.class)) {
                iTestContext.setEmptyParam();
                oRes = generateRandom(f.getAnnotation(ITestFieldClass.class).value(), map, iTestContext);
                f.set(o, oRes);
            } else if (null != fITestValue) {
                if (fITestValue.startsWith(":")) {
                    // TODO: register assignment
                    oRes=null;//TODO: implement it
                } else {
                    oRes = iTestConfig.getITestValueConverter().convert(f.getType(), fITestValue);
                    f.set(o, oRes);
                }
            } else {
                oRes = generateRandom(fType, map, iTestContext);
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

    private Class<?> getRawClass(Type fType) {
        Class<?> res;
        if (fType instanceof Class) {
            res = (Class<?>) fType;
        } else if (fType instanceof ParameterizedType) {
            res = getRawClass(((ParameterizedType) fType).getRawType());
        } else {
            throw new RuntimeException(fType.getClass() + " not supported");
        }
        return res;
    }

    protected Object fillMap(Object o, Type type, Map<String, Type> map, ITestContext iTestContext) {
        ITestParamState iTestState = iTestContext.getCurrentParam();
        Map<Object, Object> m = (Map<Object, Object>) o;
        if (null == m) {
            m = new HashMap<Object, Object>();
        } else {
            m.clear();
        }
        int size = random.nextInt(RANDOM_MAX - RANDOM_MIN) + RANDOM_MIN;
        if (null != iTestState && iTestState.getSizeParam() != null) {
            size = iTestState.getSizeParam();
        }
        for (int i = 0; i < size; i++) {
            iTestContext.enter(m, String.valueOf(i));
            iTestContext.enter("Map.Entry","key");
            ITestParamState eITestState = iTestState == null ? null : iTestState.getElement(String.valueOf(i));
            Object key = generateRandom(((ParameterizedType) type).getActualTypeArguments()[0], map, iTestContext);
            iTestContext.leave(key);
            iTestContext.enter("Map.Entry","value");
            Object value = generateRandom(((ParameterizedType) type).getActualTypeArguments()[1], map, iTestContext);
            iTestContext.leave(value);
            m.put(key, value);
            iTestContext.leave("Map.Entry");
        }
        return m;
    }

    protected Object fillCollection(Object o, Type type, Map<String, Type> map, ITestContext iTestContext) {
        ITestParamState iTestState = iTestContext.getCurrentParam();
        Collection<Object> col = (Collection<Object>) o;
        if (null == col) {
            Class<?> collectionClass;
            if (type instanceof Class<?>) {
                collectionClass = (Class<?>) type;
            } else {
                collectionClass = (Class<?>) ((ParameterizedType) type).getRawType();
            }
            if (Set.class.isAssignableFrom(collectionClass)) {
                col = new HashSet<Object>();
            } else {
                col = new ArrayList<Object>();
            }
        } else {
            col.clear();
        }
        int size = random.nextInt(RANDOM_MAX - RANDOM_MIN) + RANDOM_MIN;
        if (null != iTestState && iTestState.getSizeParam() != null) {
            size = iTestState.getSizeParam();
        }

        for (int i = 0; i < size; i++) {
            iTestContext.enter(col, String.valueOf(i));
            ITestParamState elementITestState = iTestState == null ? null : iTestState.getElement(String.valueOf(i));
            Object value = generateRandom(((ParameterizedType) type).getActualTypeArguments()[0], map, iTestContext);
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

    private static Type getTypeProxy(Type type, Map<String, Type> map) {
        if (type instanceof ParameterizedType) {
            return new ParameterizedTypeImpl((ParameterizedType) type, map);
        }
        if (type instanceof GenericArrayType) {
            return new GenericArrayTypeImpl((GenericArrayType) type, map);
        }
        if (type instanceof TypeVariable) {
            TypeVariable<?> tv = (TypeVariable<?>) type;
            log("typeVariable:" + type + ":" + tv.getGenericDeclaration());
            Type res = map.get(type.toString());
            if (res == null) {
                // log("typeVariable not found, using Object.class");
                // res = Serializable.class;
                throw new RuntimeException("Type Variable not found in map: " + type);
            }
            return res;
        }
        if (type instanceof Class) {
            return type;
        }
        throw new ITestException("Type not supported " + type.getClass());
    }

    private static void log(String log) {
        // System.out.println(log);
    }

    public interface Builder {
        boolean ignore(Class<?> clazz);

        Object build(Type type);

        Object build(Class<?> clazz);
    }

    static class ParameterizedTypeImpl implements ParameterizedType {
        private final ParameterizedType orig;

        private final Map<String, Type> map;

        ParameterizedTypeImpl(ParameterizedType orig, Map<String, Type> map) {
            this.orig = orig;
            this.map = map;
        }

        @Override
        public Type[] getActualTypeArguments() {
            Type[] types = orig.getActualTypeArguments();
            for (int i = 0; i < types.length; i++) {
                Type t = map.get(types[i].toString());
                if (t != null) {
                    types[i] = t;
                } else {
                    types[i] = getTypeProxy(types[i], map);
                }
            }
            return types;
        }

        @Override
        public Type getRawType() {
            return orig.getRawType();
        }

        @Override
        public Type getOwnerType() {
            return orig.getOwnerType();
        }

    }

    static class GenericArrayTypeImpl implements GenericArrayType {
        private final GenericArrayType orig;

        private final Map<String, Type> map;

        GenericArrayTypeImpl(GenericArrayType orig, Map<String, Type> map) {
            this.orig = orig;
            this.map = map;
        }

        @Override
        public Type getGenericComponentType() {
            Type type = map.get(orig.toString().substring(0, orig.toString().length() - 2));
            if (type == null) {
                type = getTypeProxy(orig.getGenericComponentType(), map);
            }
            return type;
        }
    }

}
