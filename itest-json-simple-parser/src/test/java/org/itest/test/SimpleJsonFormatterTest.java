package org.itest.test;

import org.itest.json.simple.impl.SimpleJsonFormatter;
import org.junit.Test;

import java.util.*;

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

    @Test
    public void test2() {
        A a = new A();
        a.b = new B();
        a.b.t = new C();
        a.b.t.s = "abcd";
        SimpleJsonFormatter f = new SimpleJsonFormatter();
        StringBuilder sb = new StringBuilder();
        f.format(a,sb);
        System.out.println(sb);
    }

    class A {
        B<C> b;
        Object object=Arrays.asList("a","b","c");
    }

    class B<T> {
        T t;
    }

    class C {
        String s;
    }
    @Test
    public void test3(){
        Array a = new Array();
        a.f=new double[]{1.1,2.2,3.3};
        a.array =new Array();
        a.array.f=a.f;
        SimpleJsonFormatter f = new SimpleJsonFormatter();
        StringBuilder sb = new StringBuilder();
        f.format(a,sb);
        System.out.println(sb);
    }
    class Array{
        double[] f;
        Array array;
    }
}
