package org.itest.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.itest.json.simple.impl.SimpleJsonFormatter;
import org.junit.Test;

public class SimpleJsonFormatterTest {
    @Test
    public void test() {
        SimpleJsonFormatter f = new SimpleJsonFormatter();
        StringBuilder sb = new StringBuilder();
        O o = new O();
        O oc = new O();
        o.o = Arrays.asList(oc);
        oc.o = Arrays.asList(o);
        f.format(o, sb);
        // System.out.println(sb.toString());
    }

    class O {
        String s = "s";

        int a = 1, b = 2, c = 3;

        Collection<O> o = new ArrayList<O>();
    }
}
