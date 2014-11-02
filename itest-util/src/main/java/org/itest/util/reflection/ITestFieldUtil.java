package org.itest.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeSet;

/**
 * Created by rumcajs on 10/31/14.
 */
public class ITestFieldUtil {
    private static Comparator<? super FieldHolder> fieldComparator = new Comparator<FieldHolder>() {
        @Override
        public int compare(FieldHolder o1, FieldHolder o2) {
            return o1.field.getName().compareTo(o2.field.getName());
        }
    };

    public static Collection<FieldHolder> collectFields(Class<?> clazz, Map<String, Type> map) {
        Collection<FieldHolder> res = new TreeSet<FieldHolder>(fieldComparator);
        Class<?> t = clazz;
        do {
            for (Field f : t.getDeclaredFields()) {
                if ( !Modifier.isStatic(f.getModifiers()) && !"this$0".equals(f.getName()) ) {
                    res.add(new FieldHolder(f, map));
                }
            }
            map = ITestTypeUtil.getTypeMap(clazz, map);
        } while ((t = t.getSuperclass()) != null);

        return res;
    }

    protected Comparator<? super FieldHolder> getFieldComparator() {
        return fieldComparator;
    }

    public static class FieldHolder {
        private Field field;

        private Map<String, Type> map;

        public FieldHolder(Field field, Map<String, Type> map) {
            this.field = field;
            this.map = map;
        }

        public Field getField() {
            return field;
        }

        public Map<String, Type> getTypeMap() {
            return map;
        }
    }

}
