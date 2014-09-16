package org.itest.test.example7;

import java.util.List;

public class ExternalTestDefinition {

    public int sum(List<Integer> list) {
        int res = 0;
        for (Integer i : list) {
            res += i;
        }
        return res;
    }
}
