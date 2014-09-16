package org.itest.impl.util;

import java.lang.reflect.Method;

public class ITestUtils {
    public static String getMethodSingnature(Method m, boolean full) {
        StringBuilder sb = new StringBuilder().append(m.getName()).append("(");
        if ( full ) {
            for (Class<?> clazz : m.getParameterTypes()) {
                sb.append(clazz.getName()).append(',');
            }
            if ( ',' == sb.charAt(sb.length() - 1) ) {
                sb.deleteCharAt(sb.length() - 1);
            }
        } else {
            sb.append('*');
        }
        sb.append(")");
        return sb.toString();
    }
}
