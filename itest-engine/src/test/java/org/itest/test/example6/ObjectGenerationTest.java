package org.itest.test.example6;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.itest.ITestConfig;
import org.itest.ITestConstants;
import org.itest.config.ITestConfigImpl;
import org.itest.impl.ITestContextImpl;
import org.itest.param.ITestParamMerger;
import org.itest.param.ITestParamParser;
import org.itest.param.ITestParamState;
import org.junit.Assert;
import org.junit.Test;

public class ObjectGenerationTest {
    @Test
    public void test() {
        ITestConfig itestConfig = new ITestConfigImpl();
        ITestParamParser parser = itestConfig.getITestParamParser();
        ITestParamState groupParams = parser.parse("T:{name:g1}");
        ITestParamState p1 = parser.parse("T:{name:p1}");
        ITestParamState p2 = parser.parse("T:{name:p2}");
        ITestParamState p3 = parser.parse("T:{name:p3}");

        Collection<String> transformations = new ArrayList<String>();
        Collection<ITestParamState> params = new ArrayList<ITestParamState>();
        transformations.add("");
        params.add(groupParams);
        transformations.add("T:persons:0=T");
        params.add(p1);
        transformations.add("T:persons:1=T");
        params.add(p2);
        ITestParamMerger merger = itestConfig.getITestParamsMerger();
        ITestParamState mergedParams = merger.merge(transformations, params).getElement(ITestConstants.THIS);

        Group g = (Group) itestConfig.getITestObjectGenerator().generate(Group.class, mergedParams, Collections.EMPTY_MAP,
                new ITestContextImpl(Collections.EMPTY_MAP));
        Assert.assertEquals("g1", g.name);
        Assert.assertEquals(2, g.persons.size());
        Assert.assertEquals("p2", g.persons.get(1).name);
    }

    static class Group {
        String name;

        List<Person> persons;
    }

    static class Person {
        String name;
    }
}
