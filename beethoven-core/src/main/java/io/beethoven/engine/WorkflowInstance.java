/**
 * The MIT License
 * Copyright © 2018 Davi Monteiro
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.beethoven.engine;

import io.beethoven.engine.core.ReporterActor;
import io.beethoven.engine.core.support.WorkflowInstanceActor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Davi Monteiro
 */
@Data
@NoArgsConstructor
public class WorkflowInstance {

    private String workflowName;
    private String workflowInstanceName;
    private Map<String, TaskInstance> tasks = new ConcurrentHashMap<>();
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private WorkflowStatus status;
    private int countTasks;

    public WorkflowInstance(@NonNull WorkflowInstanceActor.WorkflowInstanceCommand workflowInstanceCommand) {
        this.workflowName = workflowInstanceCommand.getWorkflowName();
        this.workflowInstanceName = workflowInstanceCommand.getWorkflowInstanceName();
    }

    public WorkflowInstance(@NonNull ReporterActor.ReportWorkflowEvent reportWorkflowEvent) {
        this.workflowName = reportWorkflowEvent.getWorkflowName();
        this.workflowInstanceName = reportWorkflowEvent.getWorkflowInstanceName();
    }

    public Duration elapsedTime() {
        Duration duration = Duration.ZERO;
        if (nonNull(startTime) && nonNull(endTime)) {
            duration = Duration.between(startTime, endTime);
        }
        return duration.abs();
    }

    public boolean isTerminated() {
        return countTasks == tasks.values().stream().filter(TaskInstance::isTerminated).count();
    }

    public boolean isSuccessfullyExecuted() {
        return isTerminated() && tasks.values().stream().filter(TaskInstance::isSuccessfullyExecuted).count() == countTasks;
    }

    public void print() {
        System.err.println("Workflow name: " + workflowName);
        System.err.println("Instance name: " + workflowInstanceName);
        System.err.println("Execution time: " + elapsedTime().toMillis() + " milliseconds");
        System.err.println("Success: " + isSuccessfullyExecuted());
        //tasks.values().forEach(TaskInstance::print);
    }

    public enum WorkflowStatus {
        SCHEDULED,
        RUNNING,
        PAUSED,
        COMPLETED,
        CANCELLED,
        FAILED
    }

}