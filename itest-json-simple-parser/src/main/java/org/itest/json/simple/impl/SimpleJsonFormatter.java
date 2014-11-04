package org.itest.json.simple.impl;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.itest.util.reflection.ITestFieldUtil;
import org.itest.util.reflection.ITestFieldUtil.FieldHolder;
import org.itest.util.reflection.ITestTypeUtil;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class SimpleJsonFormatter {
    public static final Comparator<Field> FIELD_NAME_COMPARATOR = new Comparator<Field>() {

        @Override
        public int compare(Field o1, Field o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    public void format(Object o, Appendable out) {
        try {
            List<String> stack = new ArrayList<String>();
            stack.add("T");
            Map<String, Type> map = ITestTypeUtil.getTypeMap((Type) o.getClass(), new HashMap<String, Type>());
            format(o, Object.class, map, out, "\t", "\n", stack, new IdentityHashMap<Object, List<String>>());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void format(Object o, Type expectedType, Map<String, Type> typeMap, Appendable out, String indent, String newLine, List<String> stack,
                        Map<Object, List<String>> visited) throws Exception {
        if (stack.size() > 20) {
            System.out.println(stack);
        }
        List<String> path;
        if (formatValue(o, expectedType, out)) {

        } else if (null != (path = visited.get(o))) {
            out.append("{@ref:").append(formatPath(stack, path)).append("}");
        } else {
            visited.put(o, new ArrayList<String>(stack));
            if(o.getClass().isArray()) {
                Type elementType;
                if(expectedType instanceof GenericArrayType){
                    elementType=ITestTypeUtil.getTypeProxy(((GenericArrayType)expectedType).getGenericComponentType(),typeMap);
                }else{
                    elementType=ITestTypeUtil.getRawClass(expectedType).getComponentType();
                }
                int length= Array.getLength(o);
                if(0==length){
                    out.append("[]");
                }else{
                    out.append("[");
                    for(int i=0;i<length;i++){
                        if(i>0){
                            out.append(',');
                        }
                        stack.add(String.valueOf(i));
                        format(Array.get(o,i),elementType,typeMap,out,indent,newLine,stack,visited);
                        stack.remove(stack.size()-1);
                    }
                    out.append("]");
                }
            }else if (o instanceof Collection) {
                Collection<Object> list = (Collection) o;
                Type elementType;
                elementType = defaultIfNull(ITestTypeUtil.getParameterType(expectedType, Collection.class, 0, typeMap), Object.class);
                elementType = ITestTypeUtil.getTypeProxy(elementType, typeMap);

                Type mapType = ITestTypeUtil.getRawClass(ITestTypeUtil.getTypeProxy(expectedType, typeMap));
                if ( mapType != o.getClass() ) {
                    out.append("{@class:").append(o.getClass().getName()).append(",_:");
                }

                if (0 == list.size()) {
                    out.append("[]");
                } else {
                    out.append('[');
                    int i = 0;
                    for (Object element : list) {
                        if (i > 0) {
                            out.append(',');
                        }
                        stack.add(String.valueOf(i++));
                        format(element, elementType, typeMap, out, indent, newLine, stack, visited);
                        stack.remove(stack.size() - 1);
                    }
                    out.append(']');
                }
                if ( mapType != o.getClass() ) {
                    out.append("}");
                }
            } else if (o instanceof Map) {
                Type mapType = ITestTypeUtil.getRawClass(ITestTypeUtil.getTypeProxy(expectedType, typeMap));
                if ( mapType != o.getClass() ) {
                    out.append("{@class:").append(o.getClass().getName()).append(",_:");
                }
                Map<Object, Object> map = (Map<Object, Object>) o;
                if (0 == map.size()) {
                    out.append("[]");
                } else {
                    out.append('[');
                    Type keyType = defaultIfNull(ITestTypeUtil.getParameterType(expectedType, Map.class, 0, typeMap), Object.class);
                    Type valueType = defaultIfNull(ITestTypeUtil.getParameterType(expectedType, Map.class, 1, typeMap), Object.class);
                    List<Map.Entry<Object, Object>> entries = new ArrayList<Map.Entry<Object, Object>>(map.entrySet());
                    for (int i = 0; i < map.size(); i++) {
                        if (i > 0) {
                            out.append(',');
                        }
                        stack.add(String.valueOf(i));
                        out.append("{key:");
                        stack.add("key");
                        format(entries.get(i).getKey(), keyType, typeMap, out, indent, newLine, stack, visited);
                        stack.remove(stack.size() - 1);
                        out.append(",value:");
                        stack.add("value");
                        format(entries.get(i).getValue(), valueType, typeMap, out, indent, newLine, stack, visited);
                        out.append("}");
                        stack.remove(stack.size() - 1);
                        stack.remove(stack.size() - 1);

                    }
                    out.append(']');
                }
                if ( mapType != o.getClass() ) {
                    out.append("}");
                }
            } else {
                try {
                    typeMap = ITestTypeUtil.getTypeMap(expectedType, typeMap);
                } catch (RuntimeException e) {
                    throw e;
                }
                Collection<FieldHolder> fields = ITestFieldUtil.collectFields(o.getClass(), typeMap);
                if (0 == fields.size() && o.getClass() == expectedType) {
                    out.append("{}");
                } else {
                    out.append('{').append(newLine);
                    boolean separator = false;
                    if (o.getClass() != ITestTypeUtil.getRawClass(expectedType)) {
                        for (int i = 0; i < stack.size(); i++) {
                            out.append(indent);
                        }
                        out.append("@class:").append(o.getClass().getName());
                        separator = true;
                    }
                    for (FieldHolder f : fields) {
                        f.getField().setAccessible(true);
                        if (!separator) {
                            separator = true;
                        } else {
                            out.append(',').append(newLine);
                        }
                        for (int i = 0; i < stack.size(); i++) {
                            out.append(indent);
                        }
                        out.append(f.getField().getName()).append(':');
                        stack.add(f.getField().getName());
                        Type fType = ITestTypeUtil.getTypeProxy(f.getField().getGenericType(), f.getTypeMap());
                        format(f.getField().get(o), fType, f.getTypeMap(), out, indent, newLine, stack, visited);
                        stack.remove(stack.size() - 1);
                    }
                    out.append(newLine);
                    for (int i = 0; i < stack.size() - 1; i++) {
                        out.append(indent);
                    }
                    out.append('}');
                }
            }
        }
    }

    private <T> T defaultIfNull(T object, T defaultObject) {
        return object == null ? defaultObject : object;
    }

    private String formatPath(List<String> stack, List<String> path) {
        int start = 0;
        while (start < path.size() && stack.get(start).equals(path.get(start))) {
            start++;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < stack.size(); i++) {
            sb.append("../");
        }
        for (int i = start; i < path.size() - 1; i++) {
            sb.append(path.get(i)).append("/");
        }
        sb.append(path.get(path.size() - 1));
        return sb.toString();
    }

    private Set<Field> collectFields(Object o) {
        Set<Field> fields = new TreeSet<Field>(FIELD_NAME_COMPARATOR);
        Class<?> c = o.getClass();
        do {
            for (Field field : c.getDeclaredFields()) {
                if (!"this$0".equals(field.getName()) && !Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    fields.add(field);
                }
            }
        } while (null != (c = c.getSuperclass()));
        return fields;
    }

    private boolean formatValue(Object object, Type t, Appendable out) throws IOException {
        boolean res = true;
        if (null == object) {
            out.append("null");
        } else {
            String value = null;
            if (object instanceof Number || object instanceof Boolean) {
                value = object.toString();
            } else if (object instanceof Character || object instanceof String) {
                value = '\'' + StringEscapeUtils.escapeJava(object.toString()) + '\'';
            } else if (object instanceof Date) {
                value = String.valueOf(((Date) object).getTime());
            } else if (object instanceof Enum) {
                value = ((Enum<?>) object).name();
            } else {
                res = false;
            }
            if (res) {
                if (getWrapper(t) != object.getClass()) {
                    out.append("{@class:").append(object.getClass().getName()).append(",_:").append(value).append("}");
                } else {
                    out.append(value);
                }
            }
        }
        return res;
    }

    private Type getWrapper(Type t) {
        if (t instanceof Class) {
            if (((Class) t).isPrimitive()) {
                t = ClassUtils.primitiveToWrapper((Class) t);
            }
        }
        return t;
    }
}
