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


import lombok.*;

import static io.beethoven.dsl.Command.CommandOperation.*;

/**
 * @author Davi Monteiro
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Command {

    private CommandOperation operation;
    private String taskName;
    private String workflowName;
    private String input;

    public enum CommandOperation {
        START_TASK(""), SCHEDULE_WORKFLOW(""), START_WORKFLOW(""), STOP_WORKFLOW(""), CANCEL_WORKFLOW("");

        @Getter
        private String commandFunction;

        CommandOperation(String commandFunction) {
            this.commandFunction = commandFunction;
        }
    }

    public static class CommandFactory {

        public static final String START_TASK = "startTask";
        public static final String START_WORKFLOW = "startWorkflow";
        public static final String STOP_WORKFLOW = "stopWorkflow";
        public static final String CANCEL_WORKFLOW = "cancelWorkflow";

        public static Command createCommand(String commandFunction, String arg) {
            Command command = null;

            switch (commandFunction) {
                case START_TASK:
                    command = startTask(arg);
                    break;

                case START_WORKFLOW:
                    command = startWorkflow(arg);
                    break;

                case STOP_WORKFLOW:
                    command = stopWorkflow(arg);
                    break;

                case CANCEL_WORKFLOW:
                    command = cancelWorkflow(arg);
                    break;
            }

            return command;
        }
    }


    public static Command startTask(@NonNull String taskName) {
        return Command.builder().taskName(taskName).operation(START_TASK).build();
    }

    public static Command startWorkflow(@NonNull String workflowName) {
        return Command.builder().workflowName(workflowName).operation(START_WORKFLOW).build();
    }

    public static Command stopWorkflow(@NonNull String workflowName) {
        return Command.builder().workflowName(workflowName).operation(STOP_WORKFLOW).build();
    }

    public static Command cancelWorkflow(@NonNull String workflowName) {
        return Command.builder().workflowName(workflowName).operation(CANCEL_WORKFLOW).build();
    }

}
