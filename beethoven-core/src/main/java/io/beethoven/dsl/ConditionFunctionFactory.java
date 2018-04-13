/**
 * The MIT License
 * Copyright © 2018 Davi Monteiro
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.beethoven.dsl;

import io.beethoven.dsl.Condition.ConditionTaskNameEqualsTo;

import static io.beethoven.dsl.Condition.ConditionWorkflowNameEqualsTo;

/**
 * @author Davi Monteiro
 */
public class ConditionFunctionFactory {

    public static final String TASK_NAME_EQUALS_TO = "taskNameEqualsTo";
    public static final String WORKFLOW_NAME_EQUALS_TO = "workflowNameEqualsTo";
    public static final String TASK_RESPONSE_EQUALS_TO = "taskResponseEqualsTo";

    public static Condition createCondition(String fuction, String arg) {
        Condition condition = new Condition();

        switch (fuction) {
            case TASK_NAME_EQUALS_TO:
                ConditionTaskNameEqualsTo conditionTaskNameEqualsTo = new ConditionTaskNameEqualsTo();
                conditionTaskNameEqualsTo.setTaskName(arg);
                condition.setConditionFunction(conditionTaskNameEqualsTo);
                break;

            case WORKFLOW_NAME_EQUALS_TO:
                ConditionWorkflowNameEqualsTo conditionWorkflowNameEqualsTo = new ConditionWorkflowNameEqualsTo();
                conditionWorkflowNameEqualsTo.setWorkflowName(arg);
                condition.setConditionFunction(conditionWorkflowNameEqualsTo);
                break;

            case TASK_RESPONSE_EQUALS_TO:
                break;
        }

        return condition;
    }

}
