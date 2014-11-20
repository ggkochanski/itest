package org.itest.scenario;

import java.util.List;

/**
 * Created by rumcajs on 11/13/14.
 */
public class ITestScenario<A extends ITestScenarioAction> {

    private List<Action> actions;

    public List<Action> getActions() {
        return actions;
    }

    public class Action {
        A action;

        String[] params;

        String result;

        public A getITestAction() {
            return action;
        }

        public String[] getParams() {
            return params;
        }

        public String getResult() {
            return result;
        }
    }
}
