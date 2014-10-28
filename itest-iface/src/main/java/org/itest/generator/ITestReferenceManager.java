package org.itest.generator;

import org.itest.param.ITestParamState;

/**
 * Created by rumcajs on 10/28/14.
 */
public interface ITestReferenceManager {
    void registerReference(ITestParamState source, Object value);

    Object getReference(ITestParamState target);

}
