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

import com.jayway.jsonpath.JsonPath;
import io.beethoven.engine.core.DeciderActor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hamcrest.Matcher;

import java.io.Serializable;

import static java.util.Objects.nonNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Condition implements Serializable {

    private ConditionFunction conditionFunction;

    public interface ConditionFunction {
        Boolean evaluate();
    }

    @Data
    public static class ConditionWorkflowNameEqualsTo implements ConditionFunction {
        private String workflowName;
        private String currentWorkflowName;

        public Boolean evaluate() {
            Boolean result = Boolean.FALSE;

            if (nonNull(workflowName) && nonNull(currentWorkflowName)) {
                result = workflowName.equals(currentWorkflowName);
            }

            return result;
        }
    }

    @Data
    public static class ConditionTaskNameEqualsTo implements ConditionFunction {
        private String taskName;
        private String currentTaskName;

        public Boolean evaluate() {
            Boolean result = Boolean.FALSE;

            if (nonNull(taskName) && nonNull(currentTaskName)) {
                result = taskName.equals(currentTaskName);
            }

            return result;
        }
    }

    @Data
    public class ConditionTaskResponseEqualsTo implements ConditionFunction {
        private ConditionFunction operator;
        private String jsonPath;
        private Matcher matcher;
        private String currentResponse;

        public Boolean evaluate() {
            Boolean result = Boolean.FALSE;

            if (nonNull(matcher) && nonNull(currentResponse)) {
                String path = JsonPath.compile(currentResponse).getPath();
                result = matcher.matches(path);
            }

            return result;
        }
    }

    public void setCurrentlValues(DeciderActor.WorkflowEvent workflowEvent) {
        if (nonNull(conditionFunction) && conditionFunction instanceof ConditionWorkflowNameEqualsTo) {
            ((ConditionWorkflowNameEqualsTo) conditionFunction).currentWorkflowName = workflowEvent.getWorkflowName();
        }
    }

    public void setCurrentlValues(DeciderActor.TaskEvent taskEvent) {
        if (nonNull(conditionFunction) && conditionFunction instanceof ConditionTaskNameEqualsTo) {
            ((ConditionTaskNameEqualsTo) conditionFunction).currentTaskName  = taskEvent.getTaskName();
        }
    }

    public Boolean evaluate() {
        return conditionFunction.evaluate();
    }

}
