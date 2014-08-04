itest
=====

Java Inline Test Framework



Simple Example usage:
--------------------

    @ITests(@ITest(init = "A:[3,16]", verify = "R:19"))
    public int sum(int a, int b) {
        return a + b;
    }

https://github.com/ggkochanski/itest/blob/master/itest-engine/src/test/java/org/itest/test/example1/SimpleExample.java

Simple Example execution:
------------------------

        Assert.assertEquals("", executor.performTestsFor(SimpleExample.class));

https://github.com/ggkochanski/itest/blob/master/itest-engine/src/test/java/org/itest/test/ITestExecutorTest.java

