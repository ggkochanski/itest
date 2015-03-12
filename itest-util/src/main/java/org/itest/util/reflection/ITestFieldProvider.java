package org.itest.util.reflection;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Created by rumcajs on 10/31/14.
 */
public class ITestFieldProvider {
    private static Comparator<? super FieldHolder> fieldComparator = new Comparator<FieldHolder>() {
        @Override
        public int compare(FieldHolder o1, FieldHolder o2) {
            return o1.field.getName().compareTo(o2.field.getName());
        }
    };
    private final ITestTypeTokenProvider typeTokenProvider;

    private final Map<Type, Collection<FieldHolder>> fieldsMap = new IdentityHashMap<Type, Collection<FieldHolder>>();

    public ITestFieldProvider(ITestTypeTokenProvider typeTokenProvider) {
        this.typeTokenProvider = typeTokenProvider;
    }

    public Collection<FieldHolder> collectFields(Type type) {
        Collection<FieldHolder> res = fieldsMap.get(type);
        if (null == res) {
            res = new TreeSet<FieldHolder>(fieldComparator);
            TypeToken t = typeTokenProvider.getTypeToken(type);
            do {
                for (Field f : t.getRawType().getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) && !"this$0".equals(f.getName())) {
                        TypeToken fType = t.resolveType(f.getGenericType());
                        res.add(new FieldHolder(f, fType.getType()));
                    }
                }

            }
            while (t.getRawType().getSuperclass() != null && (t = t.getSupertype(t.getRawType().getSuperclass())) != null);

            fieldsMap.put(type, res);
        }
        return res;
    }
    protected Comparator<? super FieldHolder> getFieldComparator() {
        return fieldComparator;
    }

    public static class FieldHolder {
        private Field field;
        private Type fType;

        public FieldHolder(Field field, Type fType) {
            this.field = field;
            this.fType = fType;
        }

        public Field getField() {
            return field;
        }

        public Type getFieldType() {
            return fType;
        }
    }

}
