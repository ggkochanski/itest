package org.itest.json.simple.format;

import com.google.common.reflect.TypeToken;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.itest.util.reflection.ITestFieldUtil;
import org.itest.util.reflection.ITestFieldUtil.FieldHolder;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

    private final SimpleJsonFormatterConfig simpleFormatterConfig;

    public SimpleJsonFormatter() {
        this(new SimpleJsonFormatterConfig());
    }

    public SimpleJsonFormatter(SimpleJsonFormatterConfig simpleJsonFormatterConfig) {
        this.simpleFormatterConfig = simpleJsonFormatterConfig;
    }

    public void format(Object o, Appendable out) {
        try {
            List<String> stack = new ArrayList<String>();
            stack.add("T");
            format(o, TypeToken.of(Object.class), out, "\t", "\n", stack, new IdentityHashMap<Object, List<String>>());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void format(Object o, TypeToken expectedType, Appendable out, String indent, String newLine, List<String> stack,
                        Map<Object, List<String>> visited) throws Exception {
        if (stack.size() > 20) {
            System.out.println(stack);
        }
        List<String> path;
        if (formatValue(o, expectedType, out)) {

        } else if (null != (path = visited.get(o))) {
            out.append("{\"@ref\":\"").append(formatPath(stack, path)).append("\"}");
        } else {
            visited.put(o, new ArrayList<String>(stack));
            Class<?> targetClass = simpleFormatterConfig.translateClass(o.getClass());
            if(o.getClass().isArray()) {
                TypeToken elementType=expectedType.getComponentType();
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
                        format(Array.get(o,i),elementType,out,indent,newLine,stack,visited);
                        stack.remove(stack.size()-1);
                    }
                    out.append("]");
                }
            }else if (o instanceof Collection) {
                Collection<Object> list = (Collection) o;
                TypeToken elementType=resolveParametrizedType(expectedType,Collection.class,0);

                Type expectedRawType=expectedType.getRawType();
                if (expectedRawType!= targetClass ) {
                    out.append("{\"@class\":\"").append(targetClass.getName()).append("\",\"_\":");
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
                        format(element, elementType, out, indent, newLine, stack, visited);
                        stack.remove(stack.size() - 1);
                    }
                    out.append(']');
                }
                if ( expectedRawType != targetClass ) {
                    out.append("}");
                }
            } else if (o instanceof Map) {
                Type mapType = expectedType.getRawType();
                if ( mapType != targetClass ) {
                    out.append("{\"@class\":\"").append(targetClass.getName()).append("\",\"_\":");
                }
                Map<Object, Object> map = (Map<Object, Object>) o;
                if (0 == map.size()) {
                    out.append("[]");
                } else {
                    out.append('[');
                    TypeToken keyType = resolveParametrizedType(expectedType,Map.class,0);
                    TypeToken valueType = resolveParametrizedType(expectedType,Map.class,1);
                    List<Map.Entry<Object, Object>> entries = new ArrayList<Map.Entry<Object, Object>>(map.entrySet());
                    for (int i = 0; i < map.size(); i++) {
                        if (i > 0) {
                            out.append(',');
                        }
                        stack.add(String.valueOf(i));
                        out.append("{\"key\":");
                        stack.add("key");
                        format(entries.get(i).getKey(), keyType,  out, indent, newLine, stack, visited);
                        stack.remove(stack.size() - 1);
                        out.append(",\"value\":");
                        stack.add("value");
                        format(entries.get(i).getValue(), valueType,  out, indent, newLine, stack, visited);
                        out.append("}");
                        stack.remove(stack.size() - 1);
                        stack.remove(stack.size() - 1);

                    }
                    out.append(']');
                }
                if ( mapType != targetClass ) {
                    out.append("}");
                }
            } else {
                Collection<FieldHolder> fields = ITestFieldUtil.collectFields(o.getClass(), Collections.EMPTY_MAP);
                if (0 == fields.size() && o.getClass() == expectedType.getRawType()) {
                    out.append("{}");
                } else {
                    out.append('{').append(newLine);
                    boolean separator = false;
                    if(null == expectedType){
                        System.out.println();
                    }
                    if ( expectedType.getRawType()!= targetClass ) {
                        for (int i = 0; i < stack.size(); i++) {
                            out.append(indent);
                        }
                        out.append("\"@class\":\"").append(targetClass.getName()).append('"');
                        separator = true;
                        //type for class lost, restoring
                        //expectedType=TypeToken.of(targetClass);
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
                        out.append('"').append(f.getField().getName()).append("\":");
                        stack.add(f.getField().getName());
                        TypeToken fType = expectedType.resolveType(f.getField().getGenericType());
                        format(f.getField().get(o), fType, out, indent, newLine, stack, visited);
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

    private String formatPath(List<String> stack, List<String> path) {
        int start = 0;
        while (start < path.size() && stack.get(start).equals(path.get(start))) {
            start++;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < stack.size(); i++) {
            sb.append("../");
        }
        for (int i = start; i < path.size(); i++) {
            sb.append(path.get(i)).append("/");
        }
        if ( '/' == sb.charAt(sb.length() - 1) ) {
            sb.deleteCharAt(sb.length() - 1);
        }
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

    private boolean formatValue(Object object, TypeToken t, Appendable out) throws IOException {
        boolean res = true;
        if (null == object) {
            out.append("null");
        } else {
            String value = null;
            Class<?> clazz=object.getClass();
            if (object instanceof Number || object instanceof Boolean) {
                value = object.toString();
            } else if (object instanceof Character || object instanceof String) {
                value = '\"' + StringEscapeUtils.escapeJava(object.toString()) + '\"';
            } else if (object instanceof Date) {
                value = String.valueOf(((Date) object).getTime());
            } else if (object instanceof Enum) {
                value = '"' + ((Enum<?>) object).name() + '"';
                if(!clazz.isEnum() && null!= clazz.getSuperclass() && clazz.getSuperclass().isEnum()){
                    clazz=clazz.getSuperclass();
                }
            } else {
                res = false;
            }
            if (res) {
                if (getWrapper(t.getRawType()) != clazz) {
                    out.append("{\"@class\":\"").append(clazz.getName()).append("\",\"_\":").append(value).append("}");
                } else {
                    out.append(value);
                }
            }
        }
        return res;
    }

    TypeToken resolveParametrizedType(TypeToken typeToken,Class clazz,int param){
        TypeToken res;
        if(clazz.isAssignableFrom(typeToken.getRawType())){
            res=typeToken.getSupertype(clazz).resolveType(clazz.getTypeParameters()[param]);
        }else{
            res=TypeToken.of(Object.class);
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
