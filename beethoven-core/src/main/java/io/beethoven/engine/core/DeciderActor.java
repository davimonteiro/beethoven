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
import akka.actor.ActorSystem;
import akka.japi.pf.ReceiveBuilder;
import io.beethoven.dsl.Command;
import io.beethoven.dsl.Condition;
import io.beethoven.dsl.Handler;
import io.beethoven.dsl.Handler.EventType;
import io.beethoven.service.HandlerService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static akka.actor.ActorRef.noSender;
import static io.beethoven.engine.core.ActorPath.TASK_ACTOR;
import static io.beethoven.engine.core.ActorPath.WORKFLOW_ACTOR;
import static io.beethoven.engine.core.WorkflowActor.StartWorkflowCommand;

/**
 * @author Davi Monteiro
 */
@Component(ActorName.DECIDER_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DeciderActor extends AbstractLoggingActor {

    @Autowired
    private HandlerService handlerService;

    @Autowired
    private ActorSystem actorSystem;

    @Override
    public Receive createReceive() {
        Receive receive = ReceiveBuilder.create()

                .match(WorkflowScheduledEvent.class, this::onWorkflowScheduledEvent)
                .match(WorkflowStartedEvent.class, this::onWorkflowStartedEvent)
                .match(WorkflowCompletedEvent.class, this::onWorkflowCompletedEvent)
                .match(WorkflowStoppedEvent.class, this::onWorkflowStoppedEvent)
                .match(WorkflowFailedEvent.class, this::onWorkflowFailedEvent)
                .match(WorkflowCanceledEvent.class, this::onWorkflowCanceledEvent)

                .match(TaskStartedEvent.class, this::onTaskStartedEvent)
                .match(TaskFailedEvent.class, this::onTaskFailedEvent)
                .match(TaskCompletedEvent.class, this::onTaskCompletedEvent)
                .match(TaskTimeoutEvent.class, this::onTaskTimeoutEvent)

                .build();

        return receive;
    }

    private void onWorkflowScheduledEvent(WorkflowScheduledEvent workflowScheduledEvent) {
        log().debug("onWorkflowScheduledEvent: " + workflowScheduledEvent);
        decide(workflowScheduledEvent, EventType.WORKFLOW_SCHEDULED);
    }

    private void onWorkflowStartedEvent(WorkflowStartedEvent workflowStartedEvent) {
        log().debug("onWorkflowStartedEvent: " + workflowStartedEvent);
        decide(workflowStartedEvent, EventType.WORKFLOW_STARTED);
    }

    private void onWorkflowCompletedEvent(WorkflowCompletedEvent workflowCompletedEvent) {
        log().debug("onWorkflowCompletedEvent: " + workflowCompletedEvent);
        decide(workflowCompletedEvent, EventType.WORKFLOW_COMPLETED);
    }

    private void onWorkflowStoppedEvent(WorkflowStoppedEvent workflowStoppedEvent) {
        log().debug("onWorkflowStoppedEvent: " + workflowStoppedEvent);
        decide(workflowStoppedEvent, EventType.WORKFLOW_STOPPED);
    }

    private void onWorkflowFailedEvent(WorkflowFailedEvent workflowFailedEvent) {
        log().debug("onWorkflowFailedEvent: " + workflowFailedEvent);
        decide(workflowFailedEvent, EventType.WORKFLOW_FAILED);
    }

    private void onWorkflowCanceledEvent(WorkflowCanceledEvent workflowCanceledEvent) {
        log().debug("onWorkflowCanceledEvent: " + workflowCanceledEvent);
        decide(workflowCanceledEvent, EventType.WORKFLOW_CANCELED);
    }

    private void onTaskStartedEvent(TaskStartedEvent taskStartedEvent) {
        log().debug("onTaskStartedEvent: " + taskStartedEvent);
        decide(taskStartedEvent, EventType.TASK_STARTED);
    }

    private void onTaskFailedEvent(TaskFailedEvent taskFailedEvent) {
        log().debug("onTaskFailedEvent: " + taskFailedEvent);
        decide(taskFailedEvent, EventType.TASK_FAILED);
    }

    private void onTaskCompletedEvent(TaskCompletedEvent taskCompletedEvent) {
        log().debug("onTaskCompletedEvent: " + taskCompletedEvent);
        decide(taskCompletedEvent, EventType.TASK_COMPLETED);
    }

    private void onTaskTimeoutEvent(TaskTimeoutEvent taskTimeoutEvent) {
        log().debug("onTaskTimeoutEvent: " + taskTimeoutEvent);
        decide(taskTimeoutEvent, EventType.TASK_TIMEDOUT);
    }

    private void decide(TaskEvent taskEvent, EventType eventType) {
        List<Handler> events = handlerService.find(taskEvent.getWorkflowName(), eventType);
        for (Handler event : events) {
            event.getConditions().forEach(condition -> condition.setCurrentlValues(taskEvent));
            event.getCommands().forEach(command -> command.setWorkflowName(taskEvent.workflowName));
            evaluateConditionsAndSendCommands(event.getConditions(), event.getCommands(), taskEvent.workflowInstanceName);
        }
    }

    private void decide(WorkflowEvent workflowEvent, EventType eventType) {
        List<Handler> events = handlerService.find(workflowEvent.getWorkflowName(), eventType);
        for (Handler event : events) {
            event.getConditions().forEach(condition -> condition.setCurrentlValues(workflowEvent));
            event.getCommands().forEach(command -> command.setWorkflowName(workflowEvent.workflowName));
            evaluateConditionsAndSendCommands(event.getConditions(), event.getCommands(), workflowEvent.workflowInstanceName);
        }
    }

    private void evaluateConditionsAndSendCommands(List<Condition> conditions, List<Command> commands, String instanceName) {
        if (evaluateConditions(conditions)) {
            sendCommands(commands, instanceName);
        }
    }

    private Boolean evaluateConditions(List<Condition> conditions) {
        Boolean result = Boolean.TRUE;
        for (Condition condition : conditions) {
            result = result && condition.evaluate();
        }

        return result;
    }

    private void sendCommands(List<Command> commands, String instanceName) {
        for (Command command : commands) {
            sendCommand(command, instanceName);
        }
    }

    private void sendCommand(Command command, String instanceName) {
        switch (command.getOperation()) {
            case START_TASK:
                actorSystem.actorSelection(TASK_ACTOR)
                        .tell(new TaskActor.StartTaskCommand(command.getTaskName(), command.getWorkflowName(), instanceName), noSender());
                break;

            case SCHEDULE_WORKFLOW:
                actorSystem.actorSelection(WORKFLOW_ACTOR)
                        .tell(new WorkflowActor.ScheduleWorkflowCommand(command.getWorkflowName()), noSender());
                break;
            case START_WORKFLOW:
                actorSystem.actorSelection(WORKFLOW_ACTOR)
                        .tell(new StartWorkflowCommand(command.getWorkflowName(), instanceName), noSender());
                break;
            case STOP_WORKFLOW:
                actorSystem.actorSelection(WORKFLOW_ACTOR)
                        .tell(new WorkflowActor.StopWorkflowCommand(command.getWorkflowName(), instanceName), noSender());
                break;
            case CANCEL_WORKFLOW:
                actorSystem.actorSelection(WORKFLOW_ACTOR)
                        .tell(new WorkflowActor.CancelWorkflowCommand(command.getWorkflowName(), instanceName), noSender());
                break;
        }

    }


    /**
     * *****************************************************************************
     * <p/>
     * Task Events: TASK_STARTED, TASK_COMPLETED, TASK_TIMEDOUT, TASK_FAILED
     * <p/>
     * *****************************************************************************
     */

    @Data
    @AllArgsConstructor
    public static abstract class TaskEvent {
        private String taskName;
        private String workflowInstanceName;
        private String workflowName;
    }

    public static class TaskStartedEvent extends TaskEvent {
        public TaskStartedEvent(String taskName, String workflowInstanceName, String workflowName) {
            super(taskName, workflowInstanceName, workflowName);
        }
    }

    public static class TaskCompletedEvent extends TaskEvent {
        public TaskCompletedEvent(String taskName, String workflowInstanceName, String workflowName) {
            super(taskName, workflowInstanceName, workflowName);
        }
    }

    public static class TaskTimeoutEvent extends TaskEvent {
        public TaskTimeoutEvent(String taskName, String workflowInstanceName, String workflowName) {
            super(taskName, workflowInstanceName, workflowName);
        }
    }

    public static class TaskFailedEvent extends TaskEvent {
        public TaskFailedEvent(String taskName, String workflowInstanceName, String workflowName) {
            super(taskName, workflowInstanceName, workflowName);
        }
    }
    /*******************************************************************************/


    /**
     * *****************************************************************************
     * <p/>
     * Workflow Events: WORKFLOW_SCHEDULED, WORKFLOW_STARTED, WORKFLOW_COMPLETED
     * <p/>
     * *****************************************************************************
     */

    @Data
    @AllArgsConstructor
    public static abstract class WorkflowEvent {
        private String workflowName;
        private String workflowInstanceName;
    }

    public static class WorkflowScheduledEvent extends WorkflowEvent {
        public WorkflowScheduledEvent(String workflowName, String workflowInstanceName) {
            super(workflowName, workflowInstanceName);
        }
    }

    public static class WorkflowStartedEvent extends WorkflowEvent {
        public WorkflowStartedEvent(String workflowName, String workflowInstanceName) {
            super(workflowName, workflowInstanceName);
        }
    }

    public static class WorkflowStoppedEvent extends WorkflowEvent {
        public WorkflowStoppedEvent(String workflowName, String workflowInstanceName) {
            super(workflowName, workflowInstanceName);
        }
    }

    public static class WorkflowCompletedEvent extends WorkflowEvent {
        public WorkflowCompletedEvent(String workflowName, String workflowInstanceName) {
            super(workflowName, workflowInstanceName);
        }
    }

    public static class WorkflowFailedEvent extends WorkflowEvent {
        public WorkflowFailedEvent(String workflowName, String workflowInstanceName) {
            super(workflowName, workflowInstanceName);
        }
    }

    public static class WorkflowCanceledEvent extends WorkflowEvent {
        public WorkflowCanceledEvent(String workflowName, String workflowInstanceName) {
            super(workflowName, workflowInstanceName);
        }
    }

    /*******************************************************************************/

}
