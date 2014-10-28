package org.itest.impl;

import org.itest.ITestConstants;
import org.itest.exception.ITestException;
import org.itest.generator.ITestReferenceManager;
import org.itest.param.ITestParamState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by rumcajs on 10/28/14.
 */
public class ITestReferenceManagerImpl implements ITestReferenceManager {
    /**
     * source,target
     */
    Map<ITestParamState, ITestParamState> refs = new IdentityHashMap<ITestParamState, ITestParamState>();

    Map<ITestParamState, Object> values = new IdentityHashMap<ITestParamState, Object>();

    public ITestReferenceManagerImpl(ITestParamState root) {
        collectReferences(root);
    }

    @Override
    public void registerReference(ITestParamState source, Object value) {
        ITestParamState target = refs.get(source);
        if ( null != target ) {
            values.put(target, value);
        }
    }

    @Override
    public Object getReference(ITestParamState target) {
        if ( values.containsKey(target) ) {
            return values.get(target);
        }
        throw new ITestException("Reference for target (" + target + ") not found");
    }

    protected void collectReferences(ITestParamState root) {
        Collection<ITestReference> res = new ArrayList<ITestReference>();
        List<String> stack = new ArrayList<String>();
        collectReferences(root, res, stack);

        for (ITestReference iTestReference : res) {
            ITestParamState sourceParam = findParam(root, iTestReference.sourcePath);
            refs.put(sourceParam, iTestReference.param);
        }
    }

    private ITestParamState findParam(ITestParamState param, List<String> path) {
        for (String element : path) {
            param = param.getElement(element);
            if ( null == param ) {
                throw new ITestException("reference path not found:" + path);
            }
        }
        return param;
    }

    private void collectReferences(ITestParamState param, Collection<ITestReference> res, List<String> stack) {
        String ref = param.getAttributes().get(ITestConstants.REFERENCE_ATTRIBUTE);
        if ( null != ref ) {
            res.add(new ITestReference(param, normalizeRef(ref, stack)));
        }
        Iterable<String> names = param.getNames();
        if ( null != names ) {
            for (String name : names) {
                stack.add(name);
                collectReferences(param.getElement(name), res, stack);
                stack.remove(stack.size() - 1);
            }
        }

    }

    private List<String> normalizeRef(String ref, List<String> stack) {
        List<String> res = new ArrayList<String>(stack);
        StringTokenizer st = new StringTokenizer(ref, ITestConstants.ASSIGN_SEPARATOR);
        while (st.hasMoreTokens()) {
            String element = st.nextToken();
            if ( ITestConstants.OWNER.equals(element) ) {
                res.remove(res.size() - 1);
            } else {
                res.add(element);
            }
        }
        return res;
    }

    static class ITestReference {

        private ITestParamState param;

        private List<String> sourcePath;

        public ITestReference(ITestParamState targetParam, List<String> sourcePath) {
            this.param = targetParam;
            this.sourcePath = sourcePath;
        }
    }

}
