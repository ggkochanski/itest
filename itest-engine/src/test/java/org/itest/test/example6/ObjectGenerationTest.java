package org.itest.test.example6;

import java.util.Collections;

import org.itest.ITestConfig;
import org.itest.ITestConstants;
import org.itest.config.ITestConfigImpl;
import org.itest.impl.ITestContextImpl;
import org.itest.impl.ITestParamAssignmentImpl;
import org.itest.param.ITestParamMerger;
import org.itest.param.ITestParamParser;
import org.itest.param.ITestParamState;
import org.itest.util.generator.ITestObjectDefinition;
import org.itest.util.generator.ITestObjectGeneratorUtil;
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

        ITestParamMerger merger = itestConfig.getITestParamsMerger();
        ITestParamState mergedParams = merger.merge( //
                new ITestParamAssignmentImpl("", groupParams), //
                new ITestParamAssignmentImpl("T:persons:0=T", p1), //
                new ITestParamAssignmentImpl("T:persons:1=T", p2) //
                ).getElement(ITestConstants.THIS);

        Group g = (Group) itestConfig.getITestObjectGenerator().generate(Group.class, mergedParams, Collections.EMPTY_MAP,
                new ITestContextImpl(Collections.EMPTY_MAP));
        Assert.assertEquals("g1", g.name);
        Assert.assertEquals(2, g.persons.size());
        Assert.assertEquals("p2", g.persons.get(1).name);
    }

    @Test
    public void iTestObjectGenerationUtilTest() {
        ITestConfig iTestConfig = new ITestConfigImpl();
        Group g = ITestObjectGeneratorUtil.generateObject(Group.class, iTestConfig, //
                new ITestObjectDefinition(Group.class, "empty", ""));
        Assert.assertEquals("Empty Group", g.name);
        Assert.assertEquals(null, g.persons);

        Person p = ITestObjectGeneratorUtil.generateObject(Person.class, iTestConfig, //
                new ITestObjectDefinition(Person.class, "p2", ""));
        Assert.assertEquals("p2", p.name);
    }

    @Test
    public void iTestCompositeObjectGenerationTest() {
        ITestConfig iTestConfig = new ITestConfigImpl();
        Group g = ITestObjectGeneratorUtil.generateObject(Group.class, iTestConfig, //
                new ITestObjectDefinition(Group.class, "example1", ""), //
                new ITestObjectDefinition(Person.class, "p1", "T:persons:1=T"), //
                new ITestObjectDefinition(Person.class, "p2", "T:persons:2=T") //
                );
        Assert.assertEquals(3, g.persons.size());
        Assert.assertEquals("Person 1", g.persons.get(0).name);
        Assert.assertEquals("p1", g.persons.get(1).name);
        Assert.assertEquals("p2", g.persons.get(2).name);
    }
}
