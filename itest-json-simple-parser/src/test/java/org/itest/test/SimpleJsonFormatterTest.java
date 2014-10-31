package org.itest.test;

import org.itest.json.simple.impl.SimpleJsonFormatter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class SimpleJsonFormatterTest {
    @Test
    public void test() {
        SimpleJsonFormatter f = new SimpleJsonFormatter();
        StringBuilder sb = new StringBuilder();
        H h = new H();
        O<List<String>> o = new O<List<String>>();
        h.o = o;
        O<List<String>> oc = new O<List<String>>();
        o.o = Arrays.asList(oc);
        o.t = new ArrayList<String>();
        o.t.add("string1");
        oc.o = Arrays.asList(o);
        oc.x = new Date();
        f.format(h, sb);
        System.out.println(sb.toString());
    }

    class H {
        O<List<String>> o;
    }

    class O<T> {
        String s = "s";

        int a = 1, b = 2, c = 3;

        Object x;

        T t;

        Collection<O<T>> o = new ArrayList<O<T>>();
    }
}
