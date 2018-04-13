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
package io.beethoven.engine.core.support;


import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import io.beethoven.config.BeethovenContext;
import io.beethoven.engine.WorkflowInstance;
import io.beethoven.engine.core.ActorPath;
import io.beethoven.engine.core.DeciderActor;
import io.beethoven.engine.core.ReporterActor;
import lombok.AllArgsConstructor;
import lombok.Data;

import static io.beethoven.engine.WorkflowInstance.WorkflowStatus.*;

public class WorkflowInstanceActor extends AbstractLoggingActor {

    private WorkflowInstance workflowInstance;

    private ActorSystem actorSystem;

    public WorkflowInstanceActor() {
        actorSystem = BeethovenContext.getApplicationContext().getBean(ActorSystem.class);
    }

    @Override
    public Receive createReceive() {
        Receive receive = ReceiveBuilder.create()
                .match(CreateWorkflowInstanceCommand.class, this::onCreateWorkflowInstance)
                .match(StartWorkflowInstanceCommand.class, this::onStartWorkflowInstanceCommand)
                .match(StopWorkflowInstanceCommand.class, this::onStopWorkflowInstanceCommand)
                .match(CancelWorkflowInstanceCommand.class, this::onCancelWorkflowInstanceCommand)
                .match(CompletedWorkflowInstanceEvent.class, this::onCompletedWorkflowInstanceEvent)
                .match(FailedWorkflowInstanceEvent.class, this::onFailedWorkflowInstanceEvent)

                .build();

        return receive;
    }

    public void onCreateWorkflowInstance(CreateWorkflowInstanceCommand createWorkflowInstance) {
        log().debug("onCreateWorkflowInstance: " + createWorkflowInstance);
        this.workflowInstance = new WorkflowInstance();
        this.workflowInstance.setWorkflowName(createWorkflowInstance.getWorkflowName());
        this.workflowInstance.setInstanceName(createWorkflowInstance.getInstanceName());
        sendEvent(new DeciderActor.WorkflowScheduledEvent(workflowInstance.getWorkflowName(), workflowInstance.getInstanceName()));
        sendEvent(new ReporterActor.ReportWorkflowScheduledEvent(workflowInstance.getWorkflowName(), workflowInstance.getInstanceName()));
    }

    public void onStartWorkflowInstanceCommand(StartWorkflowInstanceCommand startWorkflowInstanceCommand) {
        log().debug("onStartWorkflowInstanceCommand: " + startWorkflowInstanceCommand);
        this.workflowInstance.setStatus(RUNNING);
        sendEvent(new DeciderActor.WorkflowStartedEvent(workflowInstance.getWorkflowName(), workflowInstance.getInstanceName()));
        sendEvent(new ReporterActor.ReportWorkflowStartedEvent(workflowInstance.getWorkflowName(), workflowInstance.getInstanceName()));
    }

    public void onStopWorkflowInstanceCommand(StopWorkflowInstanceCommand stopWorkflowInstanceCommand) {
        log().debug("onStopWorkflowInstanceCommand: " + stopWorkflowInstanceCommand);
        this.workflowInstance.setStatus(PAUSED);
        sendEvent(new DeciderActor.WorkflowStoppedEvent(workflowInstance.getWorkflowName(), workflowInstance.getInstanceName()));
        sendEvent(new ReporterActor.ReportWorkflowStoppedEvent(workflowInstance.getWorkflowName(), workflowInstance.getInstanceName()));
    }

    public void onCompletedWorkflowInstanceEvent(CompletedWorkflowInstanceEvent completedWorkflowInstanceEvent) {
        log().debug("onCompletedWorkflowInstanceEvent: " + completedWorkflowInstanceEvent);
        this.workflowInstance.setStatus(COMPLETED);
        sendEvent(new DeciderActor.WorkflowCompletedEvent(workflowInstance.getWorkflowName(), workflowInstance.getInstanceName()));
        sendEvent(new ReporterActor.ReportWorkflowCompletedEvent(workflowInstance.getWorkflowName(), workflowInstance.getInstanceName()));
    }

    public void onCancelWorkflowInstanceCommand(CancelWorkflowInstanceCommand cancelWorkflowInstanceCommand) {
        log().debug("onCancelWorkflowInstanceCommand: " + cancelWorkflowInstanceCommand);
        this.workflowInstance.setStatus(CANCELLED);
        sendEvent(new DeciderActor.WorkflowCanceledEvent(workflowInstance.getWorkflowName(), workflowInstance.getInstanceName()));
        sendEvent(new ReporterActor.ReportWorkflowCanceledEvent(workflowInstance.getWorkflowName(), workflowInstance.getInstanceName()));
    }

    public void onFailedWorkflowInstanceEvent(FailedWorkflowInstanceEvent failedWorkflowInstanceEvent) {
        log().debug("onFailedWorkflowInstanceEvent: " + failedWorkflowInstanceEvent);
        this.workflowInstance.setStatus(FAILED);
        sendEvent(new DeciderActor.WorkflowFailedEvent(workflowInstance.getWorkflowName(), workflowInstance.getInstanceName()));
        sendEvent(new ReporterActor.ReportWorkflowFailedEvent(workflowInstance.getWorkflowName(), workflowInstance.getInstanceName()));
    }


    private void sendEvent(DeciderActor.WorkflowEvent workflowEvent) {
        actorSystem.actorSelection(ActorPath.DECIDER_ACTOR).tell(workflowEvent, ActorRef.noSender());
    }

    private void sendEvent(ReporterActor.ReportWorkflowEvent reportWorkflowEvent) {
        actorSystem.actorSelection(ActorPath.REPORT_ACTOR).tell(reportWorkflowEvent, ActorRef.noSender());
    }

    @Data
    @AllArgsConstructor
    public static abstract class WorkflowInstanceCommand {
        private String workflowName;
        private String instanceName;
    }

    public static class CreateWorkflowInstanceCommand extends WorkflowInstanceCommand {
        public CreateWorkflowInstanceCommand(String workflowName, String instanceName) {
            super(workflowName, instanceName);
        }
    }

    public static class StartWorkflowInstanceCommand extends WorkflowInstanceCommand {
        public StartWorkflowInstanceCommand(String workflowName, String instanceName) {
            super(workflowName, instanceName);
        }
    }

    public static class StopWorkflowInstanceCommand extends WorkflowInstanceCommand {
        public StopWorkflowInstanceCommand(String workflowName, String instanceName) {
            super(workflowName, instanceName);
        }
    }

    public static class CancelWorkflowInstanceCommand extends WorkflowInstanceCommand {
        public CancelWorkflowInstanceCommand(String workflowName, String instanceName) {
            super(workflowName, instanceName);
        }
    }


    public static class CompletedWorkflowInstanceEvent extends WorkflowInstanceCommand {
        public CompletedWorkflowInstanceEvent(String workflowName, String instanceName) {
            super(workflowName, instanceName);
        }
    }

    public static class FailedWorkflowInstanceEvent extends WorkflowInstanceCommand {
        public FailedWorkflowInstanceEvent(String workflowName, String instanceName) {
            super(workflowName, instanceName);
        }
    }


    public static Props props() {
        return Props.create(WorkflowInstanceActor.class);
    }

}
