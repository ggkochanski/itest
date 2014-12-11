package org.itest.scenario;

import org.itest.ITestSuperObject;
import org.itest.param.ITestParamState;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by rumcajs on 11/13/14.
 */
public class ITestScenario<A extends ITestScenarioAction> {


    private ActionHolder<Action> actions = new ActionHolder<Action>();

    public Map<String, Action> getActions() {
        return actions.getActions();
    }

    public class Action {
        A action;

        String[] params;

        String result;

        ITestParamState verify;

        public A getITestAction() {
            return action;
        }

        public String[] getParams() {
            return params;
        }

        public String getResult() {
            return result;
        }

        public ITestParamState getVerify() {
            return verify;
        }
    }

    static class ActionHolder<X> implements ITestSuperObject<X> {
        private Map<String, X> actionsMap = new LinkedHashMap<String, X>();

        @Override
        public void setField(String name, Object value) {
            actionsMap.put(name, (X) value);
        }

        public Map<String, X> getActions() {
            return actionsMap;
        }
    }
}
