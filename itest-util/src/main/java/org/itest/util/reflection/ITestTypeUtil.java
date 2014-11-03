package org.itest.util.reflection;

import org.itest.exception.ITestException;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rumcajs on 10/31/14.
 */
public class ITestTypeUtil {

    public static Type getTypeProxy(Type type, Map<String, Type> map) {
        if ( type instanceof ParameterizedType ) {
            return new ParameterizedTypeImpl((ParameterizedType) type, map);
        }
        if ( type instanceof GenericArrayType ) {
            return new GenericArrayTypeImpl((GenericArrayType) type, map);
        }
        if ( type instanceof TypeVariable ) {
            TypeVariable<?> tv = (TypeVariable<?>) type;
            Type res = map.get(type.toString());
            if ( res == null ) {
                // log("typeVariable not found, using Object.class");
                // res = Serializable.class;
        		res=Object.class;
                //throw new RuntimeException("Type Variable not found in map: " + type);
            }
            return res;
        }
        if ( type instanceof WildcardType ){
            return getTypeProxy(inferClassTypeFromWildcardType(type),map);
        }
        if ( type instanceof Class ) {
            return type;
        }
        throw new ITestException("Type not supported " + type.getClass());
    }

    public static Map<String, Type> getTypeMap(Class<?> clazz, Map<String, Type> map) {
        if ( clazz.getGenericSuperclass() instanceof ParameterizedType ) {
            Type typeActualArguments[] = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();
            TypeVariable<?> typeArguments[] = clazz.getSuperclass().getTypeParameters();
            final Map<String, Type> map2 = new HashMap<String, Type>();
            for (int i = 0; i < typeActualArguments.length; i++) {
                Type ta = map.get(typeActualArguments[i].toString());
                map2.put(typeArguments[i].getName(), ta == null ? typeActualArguments[i] : ta);
            }
            map = map2;
        }
        return map;
    }

    public static Type[] getTypeActualArguments(Type type) {
        Type[] typeActualArguments = {};
        if (  (type instanceof  ParameterizedType)) {
            typeActualArguments = ((ParameterizedType) type).getActualTypeArguments();
        }
        return typeActualArguments;
    }

    public static Map<String, Type> getTypeMap(Type type, Map<String, Type> itestGenericMap) {
        Class<?> clazz=getRawClass(type);
        Type[] typeActualArguments=getTypeActualArguments(type);
        TypeVariable<?> typeArguments[] = clazz.getTypeParameters();
        final Map<String, Type> map = new HashMap<String, Type>(itestGenericMap);
        for (int i = 0; i < typeActualArguments.length; i++) {
            map.put(typeArguments[i].getName(), typeActualArguments[i]);
            if(typeArguments[i].equals("E") && "E".equals(typeActualArguments[i].toString())){
                System.out.println();
            }
        }

        return map;
    }

    public static Type getParameterType(Type expectedType, Class<?> searchClass, int paramIndex, Map<String, Type> map) {
        if(null==expectedType){
            return null;
        }
        Class<?> rawClass=getRawClass(expectedType);
        Type res=null;
        if(rawClass==searchClass){
            if(expectedType instanceof ParameterizedType){
                res = ((ParameterizedType) getTypeProxy(expectedType, map)).getActualTypeArguments()[paramIndex];
            }
        }else{
            map = getTypeMap(expectedType, map);
            res = getParameterType(rawClass.getGenericSuperclass(), searchClass, paramIndex, map);
            if(null==res){
                for(Type type:rawClass.getGenericInterfaces()){
                    res = getParameterType(type, searchClass, paramIndex, map);
                    if(null!=res){
                        break;
                    }
                }
            }
        }

        return res;
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
            if ( type == null ) {
                type = getTypeProxy(orig.getGenericComponentType(), map);
            }
            return type;
        }
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
                if ( t != null ) {
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

    private static Type inferClassTypeFromWildcardType(Type type) {
        Type resultClass = null;
        if (type instanceof WildcardType) {
            WildcardType sampleType = (WildcardType) type;
            Type[] lowerBounds = sampleType.getLowerBounds();
            Type[] upperBounds = sampleType.getUpperBounds();
            if (lowerBounds.length != 0) {
                resultClass =  lowerBounds[0];
            } else if (upperBounds.length != 0) {
                resultClass =  upperBounds[0];
            }
        }
        return resultClass;
    }



    public static Class<?> getRawClass(Type fType) {
        Class<?> res;
        if (fType instanceof Class) {
            res = (Class<?>) fType;
        } else if (fType instanceof ParameterizedType) {
            res = getRawClass(((ParameterizedType) fType).getRawType());
//        } else if (fType instanceof TypeVariable){
//            res= ((TypeVariable)fType).getBounds()
        } else if ( fType instanceof WildcardType ) {
            res=getRawClass(inferClassTypeFromWildcardType(fType));
        } else {
            throw new RuntimeException(fType.getClass() + " not supported");
        }
        return res;
    }

}
