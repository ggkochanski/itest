/**
 * <pre>
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 Grzegorz Kochański
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * </pre>
 */
package org.itest.config;

import org.itest.ITestConfig;
import org.itest.definition.ITestDefinitionFactory;
import org.itest.execution.ITestMethodExecutor;
import org.itest.generator.ITestObjectGenerator;
import org.itest.impl.ITestDefinitionFactoryImpl;
import org.itest.impl.ITestExecutionVerifierImpl;
import org.itest.impl.ITestMethodExecutorImpl;
import org.itest.impl.ITestParamMergerImpl;
import org.itest.impl.ITestRandomObjectGeneratorImpl;
import org.itest.impl.ITestValueConverterImpl;
import org.itest.json.simple.ITestSimpleJsonParamParserImpl;
import org.itest.param.ITestParamMerger;
import org.itest.param.ITestParamParser;
import org.itest.param.ITestValueConverter;
import org.itest.verify.ITestExecutionVerifier;

public class ITestConfigImpl implements ITestConfig {
    private ITestDefinitionFactory iTestPathDefinitionFactory = new ITestDefinitionFactoryImpl(this);

    private ITestObjectGenerator iTestObjectGenerator = new ITestRandomObjectGeneratorImpl(this);

    private ITestExecutionVerifier iTestObjectVerifier = new ITestExecutionVerifierImpl(this);

    private ITestMethodExecutor iTestPathVerifier = new ITestMethodExecutorImpl(this);

    private ITestParamMerger iTestParamsMerger = new ITestParamMergerImpl();

    private ITestParamParser iTestParamsParser = new ITestSimpleJsonParamParserImpl();

    private ITestValueConverter iTestValueConverter = new ITestValueConverterImpl();

    @Override
    public ITestDefinitionFactory getITestDefinitionFactory() {
        return iTestPathDefinitionFactory;
    }

    public void setiTestDefinitionFactory(ITestDefinitionFactory iTestPathDefinitionFactory) {
        this.iTestPathDefinitionFactory = iTestPathDefinitionFactory;
    }

    @Override
    public ITestObjectGenerator getITestObjectGenerator() {
        return iTestObjectGenerator;
    }

    public void setITestObjectGenerator(ITestObjectGenerator iTestObjectGenerator) {
        this.iTestObjectGenerator = iTestObjectGenerator;
    }

    @Override
    public ITestMethodExecutor getITestMethodExecutor() {
        return iTestPathVerifier;
    }

    public void setITestMethodExecutor(ITestMethodExecutor iTestPathVerifier) {
        this.iTestPathVerifier = iTestPathVerifier;
    }

    @Override
    public ITestExecutionVerifier getITestExecutionVerifier() {
        return iTestObjectVerifier;
    }

    public void setITestExecutionVerifier(ITestExecutionVerifier iTestObjectVerifier) {
        this.iTestObjectVerifier = iTestObjectVerifier;
    }

    @Override
    public ITestParamMerger getITestParamsMerger() {
        return iTestParamsMerger;
    }

    @Override
    public ITestParamParser getITestParamParser() {
        return iTestParamsParser;
    }

    public void setITestParamMerger(ITestParamMerger iTestParamsMerger) {
        this.iTestParamsMerger = iTestParamsMerger;
    }

    public void setITestParamParser(ITestParamParser iTestParamsParser) {
        this.iTestParamsParser = iTestParamsParser;
    }

    @Override
    public ITestValueConverter getITestValueConverter() {
        return iTestValueConverter;
    }

    public void setITestValueConverter(ITestValueConverter iTestValueConverter) {
        this.iTestValueConverter = iTestValueConverter;
    }
}
