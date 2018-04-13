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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author Davi Monteiro
 */
@ToString
@EqualsAndHashCode(of = "name")
public class Handler {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private EventType eventType;

    @Getter
    @Setter
    private List<Condition> conditions;

    @Getter
    @Setter
    private List<Command> commands;

    public enum EventType {
        // Task events
        TASK_STARTED, TASK_COMPLETED, TASK_TIMEDOUT, TASK_FAILED,

        // Workflow events
        WORKFLOW_SCHEDULED, WORKFLOW_STARTED, WORKFLOW_COMPLETED, WORKFLOW_STOPPED, WORKFLOW_FAILED, WORKFLOW_CANCELED
    }

}
