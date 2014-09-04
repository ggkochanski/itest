package org.itest.json.simple.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringEscapeUtils;

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
            format(o, out, "\t", "\n", stack, new IdentityHashMap<Object, String>());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void format(Object o, Appendable out, String indent, String newLine, List<String> stack, Map<Object, String> visited) throws Exception {
        if ( stack.size() > 20 ) {
            System.out.println(stack);
        }
        String path;
        if ( formatValue(o, out) ) {

        } else if ( null != (path = visited.get(o)) ) {
            out.append(path);
        } else {
            visited.put(o, formatPath(stack));
            if ( o instanceof List ) {
                List<Object> list = (List) o;
                if ( 0 == list.size() ) {
                    out.append("[]");
                } else {
                    out.append('[');
                    for (int i = 0; i < list.size(); i++) {
                        if ( i > 0 ) {
                            out.append(',');
                        }
                        stack.add("[" + i + "]");
                        format(list.get(i), out, indent, newLine, stack, visited);
                        stack.remove(stack.size() - 1);
                    }
                    out.append(']');
                }
            } else if ( o instanceof Map ) {
                Map<Object, Object> map = (Map<Object, Object>) o;
                if ( 0 == map.size() ) {
                    out.append("[]");
                } else {
                    out.append('[');
                    List<Map.Entry<Object, Object>> entries = new ArrayList<Map.Entry<Object, Object>>(map.entrySet());
                    for (int i = 0; i < map.size(); i++) {
                        if ( i > 0 ) {
                            out.append(',');
                        }
                        stack.add("[" + i + "]");
                        out.append("{key:");
                        stack.add("key");
                        format(entries.get(i).getKey(), out, indent, newLine, stack, visited);
                        stack.remove(stack.size() - 1);
                        out.append(",value:");
                        stack.add("value");
                        format(entries.get(i).getValue(), out, indent, newLine, stack, visited);
                        out.append("}");
                        stack.remove(stack.size() - 1);
                        stack.remove(stack.size() - 1);

                    }
                    out.append(']');
                }
            } else {
                Set<Field> fields = collectFields(o);
                if ( 0 == fields.size() ) {
                    out.append("{}");
                } else {
                    out.append('{').append(newLine);
                    boolean separator = false;
                    for (Field field : fields) {
                        if ( !separator ) {
                            separator = true;
                        } else {
                            out.append(',').append(newLine);
                        }
                        for (int i = 0; i < stack.size(); i++) {
                            out.append(indent);
                        }
                        out.append(field.getName()).append(':');
                        stack.add(field.getName());
                        if ( field.getName().equals("defaultCode") ) {
                            System.out.println();
                        }
                        format(field.get(o), out, indent, newLine, stack, visited);
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
            if ( i > 0 ) {
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
                if ( !"this$0".equals(field.getName()) && !Modifier.isStatic(field.getModifiers()) ) {
                    field.setAccessible(true);
                    fields.add(field);
                }
            }
        } while (null != (c = c.getSuperclass()));
        return fields;
    }

    private boolean formatValue(Object object, Appendable out) throws IOException {
        boolean res = true;
        if ( null == object ) {
            out.append("null");
        } else if ( object instanceof Number || object instanceof Boolean ) {
            out.append(object.toString());
        } else if ( object instanceof Character || object instanceof String ) {
            out.append('\'').append(StringEscapeUtils.escapeJava(object.toString())).append('\'');
        } else if ( object instanceof Date ) {
            out.append(String.valueOf(((Date) object).getTime()));
        } else if ( object instanceof Enum ) {
            out.append(((Enum<?>) object).name());
        } else {
            res = false;
        }
        return res;
    }
}
