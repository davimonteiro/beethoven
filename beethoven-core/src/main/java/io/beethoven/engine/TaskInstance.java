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
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * @author Davi Monteiro
 */
@Data @NoArgsConstructor
public class TaskInstance {

    private String workflowName;
    private String workflowInstanceName;
    private String taskName;
    private String taskInstanceName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String response;
    private Throwable failure;

    public TaskInstance(ReporterActor.ReportTaskEvent reportTaskEvent) {
        requireNonNull(reportTaskEvent);
        this.workflowName = reportTaskEvent.getWorkflowName();
        this.workflowInstanceName = reportTaskEvent.getWorkflowInstanceName();
        this.taskName = reportTaskEvent.getTaskName();
        this.taskInstanceName = reportTaskEvent.getTaskInstanceName();
    }

    public Duration elapsedTime() {
        requireNonNull(startTime);
        requireNonNull(endTime);
        Duration duration = Duration.between(startTime, endTime);
        return duration.abs();
    }

    public boolean isTerminated() {
        return nonNull(startTime) && nonNull(endTime);
    }

    public void print() {
        System.err.println("Task instance name: " + taskInstanceName);
        System.err.println("Execution time: " + elapsedTime().getSeconds() + " seconds");
        if (nonNull(response)) System.err.println("Response: " + response);
        if (nonNull(failure)) System.err.println("Failure: " + failure.getMessage());
    }

}
