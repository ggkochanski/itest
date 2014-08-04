[Java Inline Test Framework](../../wiki)
=====
Java Inline Test Framework is designed to unit testing by data (without additional source code). Oriented on re-usage of test data definitions. Applicable for shallow (dynamic proxies) or deep (selected implementations) tests. You define tests by composing initial data and expected result data.

> Understanding the scope of source code change at early stage is a key of effective product quality management.

[Simple usage example](https://github.com/ggkochanski/itest/blob/master/itest-engine/src/test/java/org/itest/test/example1/SimpleExample.java) :
--------------------

    @ITests(@ITest(init = "A:[3,16]", verify = "R:19"))
    public int sum(int a, int b) {
        return a + b;
    }




[Simple Example execution](https://github.com/ggkochanski/itest/blob/master/itest-engine/src/test/java/org/itest/test/ITestExecutorTest.java):
------------------------

        Assert.assertEquals("", executor.performTestsFor(SimpleExample.class));



For more details visit [ITest project Wiki page](../../wiki).
