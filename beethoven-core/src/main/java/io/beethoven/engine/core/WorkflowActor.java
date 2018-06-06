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
package io.beethoven.engine.core;


import akka.actor.AbstractLoggingActor;
import akka.japi.pf.ReceiveBuilder;
import io.beethoven.engine.core.support.WorkflowInstanceActor;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author Davi Monteiro
 */
@Component(ActorName.WORKFLOW_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class WorkflowActor extends AbstractLoggingActor {

    private final AtomicInteger count = new AtomicInteger();

    @Override
    public Receive createReceive() {
        Receive receive = ReceiveBuilder.create()
                .match(ScheduleWorkflowCommand.class, this::onScheduleWorkflowCommand)
                .match(StartWorkflowCommand.class, this::onStartWorkflowCommand)
                .match(StopWorkflowCommand.class, this::onStopWorkflowCommand)
                .match(CancelWorkflowCommand.class, this::onCancelWorkflowCommand)
                .build();

        return receive;
    }

    private void onScheduleWorkflowCommand(ScheduleWorkflowCommand scheduleWorkflowCommand) {
        log().debug("onScheduleWorkflowCommand: " + scheduleWorkflowCommand);
        String instanceName = generateInstanceName(scheduleWorkflowCommand.workflowName);
        getContext().actorOf(WorkflowInstanceActor.props(), instanceName);

        forwardCommand(new WorkflowInstanceActor.CreateWorkflowInstanceCommand(scheduleWorkflowCommand.workflowName,
                instanceName), instanceName);
    }

    private void onStartWorkflowCommand(StartWorkflowCommand startWorkflowCommand) {
        log().debug("onStartWorkflowCommand: " + startWorkflowCommand);
        forwardCommand(new WorkflowInstanceActor.CreateWorkflowInstanceCommand(startWorkflowCommand.workflowName,
                startWorkflowCommand.instanceName), startWorkflowCommand.instanceName);
    }

    private void onStopWorkflowCommand(StopWorkflowCommand stopWorkflowCommand) {
        log().debug("onStopWorkflowCommand: " + stopWorkflowCommand);
        forwardCommand(new WorkflowInstanceActor.StopWorkflowInstanceCommand(stopWorkflowCommand.workflowName,
                stopWorkflowCommand.instanceName), stopWorkflowCommand.instanceName);
    }

    private void onCancelWorkflowCommand(CancelWorkflowCommand cancelWorkflowCommand) {
        log().debug("onCancelWorkflowCommand: " + cancelWorkflowCommand);
        forwardCommand(new WorkflowInstanceActor.CancelWorkflowInstanceCommand(cancelWorkflowCommand.workflowName,
                cancelWorkflowCommand.instanceName), cancelWorkflowCommand.instanceName);
    }

    private void forwardCommand(WorkflowInstanceActor.WorkflowInstanceCommand command, String actorName) {
        getContext().findChild(actorName).ifPresent(child -> child.forward(command, getContext()));
    }

    private String generateInstanceName(String workflowName) {
        return workflowName + "-" + count.incrementAndGet();
    }

    /*******************************************************************************
     *
     * Workflow Commands: SCHEDULE_WORKFLOW, START_WORKFLOW,
     *                    STOP_WORKFLOW, CANCEL_WORKFLOW
     *
     *******************************************************************************/
    public interface WorkflowCommand { }

    @Data @AllArgsConstructor
    public static class ScheduleWorkflowCommand implements WorkflowCommand {
        private String workflowName;
    }

    @Data @AllArgsConstructor
    public static class StartWorkflowCommand implements WorkflowCommand {
        private String workflowName;
        private String instanceName;
    }

    @Data @AllArgsConstructor
    public static class StopWorkflowCommand implements WorkflowCommand {
        private String workflowName;
        private String instanceName;
    }

    @Data @AllArgsConstructor
    public static class CancelWorkflowCommand implements WorkflowCommand {
        private String workflowName;
        private String instanceName;
    }
    /*******************************************************************************/

}
