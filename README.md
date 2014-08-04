itest
=====

Java Inline Test Framework



[Simple usage example](https://github.com/ggkochanski/itest/blob/master/itest-engine/src/test/java/org/itest/test/example1/SimpleExample.java) :
--------------------

    @ITests(@ITest(init = "A:[3,16]", verify = "R:19"))
    public int sum(int a, int b) {
        return a + b;
    }




[Simple Example execution](https://github.com/ggkochanski/itest/blob/master/itest-engine/src/test/java/org/itest/test/ITestExecutorTest.java):
------------------------

        Assert.assertEquals("", executor.performTestsFor(SimpleExample.class));



