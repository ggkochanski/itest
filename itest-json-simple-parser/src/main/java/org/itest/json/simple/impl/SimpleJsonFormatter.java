package org.itest.json.simple.impl;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.itest.util.reflection.ITestFieldUtil;
import org.itest.util.reflection.ITestFieldUtil.FieldHolder;
import org.itest.util.reflection.ITestTypeUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

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
            Map<String, Type> map = ITestTypeUtil.getTypeMap(o.getClass(), new HashMap<String, Type>());
            format(o, Object.class, map, out, "\t", "\n", stack, new IdentityHashMap<Object, String>());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void format(Object o, Type expectedType, Map<String, Type> typeMap, Appendable out, String indent, String newLine, List<String> stack,
                        Map<Object, String> visited) throws Exception {
        if (stack.size() > 20) {
            System.out.println(stack);
        }
        String path;
        if (formatValue(o, expectedType, out)) {

        } else if (null != (path = visited.get(o))) {
            out.append(path);
        } else {
            visited.put(o, formatPath(stack));
            if (o instanceof Collection) {
                typeMap = ITestTypeUtil.getTypeMap(o.getClass(), typeMap);
                List<Object> list = (List) o;
                if (0 == list.size()) {
                    out.append("[]");
                } else {
                    out.append('[');
                    for (int i = 0; i < list.size(); i++) {
                        if (i > 0) {
                            out.append(',');
                        }
                        stack.add("[" + i + "]");
                        //TODO: check if exepcted type is Collection
                        Type elementType = Object.class;
                        if (expectedType instanceof ParameterizedType) {
                            elementType = ((ParameterizedType) expectedType).getActualTypeArguments()[0];
                        }
                        Type proxyType = ITestTypeUtil.getTypeProxy(elementType, typeMap);
                        format(list.get(i), proxyType, typeMap, out, indent, newLine, stack, visited);
                        stack.remove(stack.size() - 1);
                    }
                    out.append(']');
                }
            } else if (o instanceof Map) {
                Map<Object, Object> map = (Map<Object, Object>) o;
                if (0 == map.size()) {
                    out.append("[]");
                } else {
                    out.append('[');
                    List<Map.Entry<Object, Object>> entries = new ArrayList<Map.Entry<Object, Object>>(map.entrySet());
                    for (int i = 0; i < map.size(); i++) {
                        if (i > 0) {
                            out.append(',');
                        }
                        stack.add("[" + i + "]");
                        out.append("{key:");
                        stack.add("key");

                        Type keyType = ((ParameterizedType) o.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
                        format(entries.get(i).getKey(), ITestTypeUtil.getRawClass(keyType), typeMap, out, indent, newLine, stack, visited);
                        stack.remove(stack.size() - 1);
                        out.append(",value:");
                        stack.add("value");
                        Type valueType = ((ParameterizedType) o.getClass().getGenericSuperclass()).getActualTypeArguments()[1];
                        format(entries.get(i).getValue(), ITestTypeUtil.getRawClass(valueType), typeMap, out, indent, newLine, stack, visited);
                        out.append("}");
                        stack.remove(stack.size() - 1);
                        stack.remove(stack.size() - 1);

                    }
                    out.append(']');
                }
            } else {
                typeMap = ITestTypeUtil.getTypeMap(expectedType, typeMap);
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

    private String formatPath(List<String> stack) {
        StringBuilder sb = new StringBuilder();
        sb.append("'=");
        for (int i = 0; i < stack.size(); i++) {
            if (i > 0) {
                sb.append(':');
            }
            sb.append(stack.get(i));
        }
        sb.append("'");
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
