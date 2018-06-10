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
package io.beethoven.engine.core;


import akka.actor.AbstractLoggingActor;
import akka.japi.pf.ReceiveBuilder;
import io.beethoven.dsl.Workflow;
import io.beethoven.engine.TaskInstance;
import io.beethoven.engine.WorkflowInstance;
import io.beethoven.repository.ContextualInputRepository;
import io.beethoven.repository.WorkflowRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static akka.actor.ActorRef.noSender;

/**
 * @author Davi Monteiro
 */
@Component(ActorName.REPORTER_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReporterActor extends AbstractLoggingActor {

    @Autowired
    private ContextualInputRepository contextualInputRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    private Map<String, WorkflowInstance> instances = new ConcurrentHashMap();

    @Override
    public Receive createReceive() {

        Receive receive = ReceiveBuilder.create()
                .match(ReportWorkflowScheduledEvent.class, this::onReportWorkflowScheduledEvent)
                .match(ReportWorkflowStartedEvent.class, this::onReportWorkflowStartedEvent)
                .match(ReportWorkflowStoppedEvent.class, this::onReportWorkflowStoppedEvent)
                .match(ReportWorkflowCompletedEvent.class, this::onReportWorkflowCompletedEvent)
                .match(ReportWorkflowCanceledEvent.class, this::onReportWorkflowCanceledEvent)
                .match(ReportWorkflowFailedEvent.class, this::onReportWorkflowFailedEvent)

                .match(ReportTaskStartedEvent.class, this::onReportTaskStartedEvent)
                .match(ReportTaskTimeoutEvent.class, this::onReportTaskTimeoutEvent)
                .match(ReportTaskFailedEvent.class, this::onReportTaskFailedEvent)
                .match(ReportTaskCompletedEvent.class, this::onReportTaskCompletedEvent)

                .build();

        return receive;
    }


    private void onReportWorkflowScheduledEvent(ReportWorkflowScheduledEvent reportWorkflowScheduledEvent) {
        log().debug("onReportWorkflowScheduledEvent: " + reportWorkflowScheduledEvent);

        Workflow workflow = workflowRepository.findByName(reportWorkflowScheduledEvent.getWorkflowName());

        WorkflowInstance workflowInstance = new WorkflowInstance(reportWorkflowScheduledEvent);
        workflowInstance.setStatus(WorkflowInstance.WorkflowStatus.SCHEDULED);
        workflowInstance.setCountTasks(workflow.getTasks().size());

        instances.put(reportWorkflowScheduledEvent.getWorkflowInstanceName(), workflowInstance);
    }

    private void onReportWorkflowStartedEvent(ReportWorkflowStartedEvent reportWorkflowStartedEvent) {
        log().debug("onReportWorkflowStartedEvent: " + reportWorkflowStartedEvent);
        WorkflowInstance workflowInstance = instances.get(reportWorkflowStartedEvent.getWorkflowInstanceName());
        workflowInstance.setStatus(WorkflowInstance.WorkflowStatus.RUNNING);
        workflowInstance.setStartTime(LocalDateTime.now());
    }

    private void onReportWorkflowStoppedEvent(ReportWorkflowStoppedEvent reportWorkflowStoppedEvent) {
        log().debug("onReportWorkflowStoppedEvent: " + reportWorkflowStoppedEvent);
        WorkflowInstance workflowInstance = instances.get(reportWorkflowStoppedEvent.getWorkflowInstanceName());
        workflowInstance.setEndTime(LocalDateTime.now());
        report(reportWorkflowStoppedEvent);
    }

    private void onReportWorkflowCompletedEvent(ReportWorkflowCompletedEvent reportWorkflowCompletedEvent) {
        log().debug("onReportWorkflowCompletedEvent: " + reportWorkflowCompletedEvent);
        WorkflowInstance workflowInstance = instances.get(reportWorkflowCompletedEvent.getWorkflowInstanceName());
        workflowInstance.setEndTime(LocalDateTime.now());
        report(reportWorkflowCompletedEvent);
    }

    private void onReportWorkflowCanceledEvent(ReportWorkflowCanceledEvent reportWorkflowCanceledEvent) {
        log().debug("onReportWorkflowCanceledEvent: " + reportWorkflowCanceledEvent);
        WorkflowInstance workflowInstance = instances.get(reportWorkflowCanceledEvent.getWorkflowInstanceName());
        workflowInstance.setEndTime(LocalDateTime.now());
        report(reportWorkflowCanceledEvent);
    }

    private void onReportWorkflowFailedEvent(ReportWorkflowFailedEvent reportWorkflowFailedEvent) {
        log().debug("onReportWorkflowFailedEvent: " + reportWorkflowFailedEvent);
        WorkflowInstance workflowInstance = instances.get(reportWorkflowFailedEvent.getWorkflowInstanceName());
        workflowInstance.setEndTime(LocalDateTime.now());
        report(reportWorkflowFailedEvent);
    }

    private void onReportTaskStartedEvent(ReportTaskStartedEvent reportTaskStartedEvent) {
        log().debug("onReportTaskStartedEvent: " + reportTaskStartedEvent);

        WorkflowInstance workflowInstance = instances.get(reportTaskStartedEvent.getWorkflowInstanceName());
        if (workflowInstance.getStatus().equals(WorkflowInstance.WorkflowStatus.SCHEDULED)) {
            self().tell(new ReportWorkflowStartedEvent(
                    reportTaskStartedEvent.getWorkflowName(),
                    reportTaskStartedEvent.getWorkflowInstanceName()), noSender());
        }

        TaskInstance taskInstance = new TaskInstance(reportTaskStartedEvent);
        taskInstance.setStartTime(LocalDateTime.now());
        workflowInstance.getTasks().put(taskInstance.getTaskInstanceName(), taskInstance);

        report(reportTaskStartedEvent);
    }

    private void onReportTaskCompletedEvent(ReportTaskCompletedEvent reportTaskCompletedEvent) {
        log().debug("onReportTaskCompletedEvent: " + reportTaskCompletedEvent);
        instances.get(reportTaskCompletedEvent.getWorkflowInstanceName())
                .getTasks().get(reportTaskCompletedEvent.getTaskInstanceName())
                .setEndTime(LocalDateTime.now());
        report(reportTaskCompletedEvent);

        WorkflowInstance workflowInstance = instances.get(reportTaskCompletedEvent.getWorkflowInstanceName());
        if (workflowInstance.isTerminated()) {
            self().tell(new ReportWorkflowCompletedEvent(
                    reportTaskCompletedEvent.getWorkflowName(),
                    reportTaskCompletedEvent.getWorkflowInstanceName()), noSender());
        }
    }

    private void onReportTaskTimeoutEvent(ReportTaskTimeoutEvent reportTaskTimeoutEvent) {
        log().debug("onReportTaskTimeoutEvent" + reportTaskTimeoutEvent);
        instances.get(reportTaskTimeoutEvent.getWorkflowInstanceName())
                .getTasks().get(reportTaskTimeoutEvent.getTaskInstanceName())
                .setEndTime(LocalDateTime.now());
        report(reportTaskTimeoutEvent);
    }

    private void onReportTaskFailedEvent(ReportTaskFailedEvent reportTaskFailedEvent) {
        log().debug("onReportTaskFailedEvent" + reportTaskFailedEvent);
        instances.get(reportTaskFailedEvent.getWorkflowInstanceName())
                .getTasks().get(reportTaskFailedEvent.getTaskInstanceName())
                .setEndTime(LocalDateTime.now());
        report(reportTaskFailedEvent);
    }

    private void clearWorkflowInstanceResources(WorkflowInstance workflowInstance) {
        contextualInputRepository.deleteLocalContextualInput(workflowInstance.getWorkflowInstanceName());
        instances.remove(workflowInstance.getWorkflowInstanceName());
    }

    // TODO Implement reporting for task events
    private void report(ReportTaskEvent reportTaskEvent) {
    }

    // TODO Implement reporting for workflow events
    private void report(ReportWorkflowEvent reportWorkflowEvent) {
        WorkflowInstance workflowInstance = instances.get(reportWorkflowEvent.workflowInstanceName);
        workflowInstance.print();
        clearWorkflowInstanceResources(workflowInstance);
    }

    /**
     * *****************************************************************************
     * <p/>
     * Workflow Events
     * <p/>
     * *****************************************************************************
     */
    @Data
    @AllArgsConstructor
    public static abstract class ReportWorkflowEvent {
        private String workflowName;
        private String workflowInstanceName;
    }

    public static class ReportWorkflowScheduledEvent extends ReportWorkflowEvent {
        public ReportWorkflowScheduledEvent(String workflowName, String instanceName) {
            super(workflowName, instanceName);
        }
    }

    public static class ReportWorkflowStartedEvent extends ReportWorkflowEvent {
        public ReportWorkflowStartedEvent(String workflowName, String instanceName) {
            super(workflowName, instanceName);
        }
    }

    public static class ReportWorkflowStoppedEvent extends ReportWorkflowEvent {
        public ReportWorkflowStoppedEvent(String workflowName, String instanceName) {
            super(workflowName, instanceName);
        }
    }

    public static class ReportWorkflowCompletedEvent extends ReportWorkflowEvent {
        public ReportWorkflowCompletedEvent(String workflowName, String instanceName) {
            super(workflowName, instanceName);
        }
    }

    public static class ReportWorkflowFailedEvent extends ReportWorkflowEvent {
        public ReportWorkflowFailedEvent(String workflowName, String instanceName) {
            super(workflowName, instanceName);
        }
    }

    public static class ReportWorkflowCanceledEvent extends ReportWorkflowEvent {
        public ReportWorkflowCanceledEvent(String workflowName, String instanceName) {
            super(workflowName, instanceName);
        }
    }

    /*******************************************************************************/


    /**
     * *****************************************************************************
     * <p/>
     * Task Events
     * <p/>
     * *****************************************************************************
     */
    @Data
    @AllArgsConstructor
    public static abstract class ReportTaskEvent {
        private String workflowName;
        private String workflowInstanceName;
        private String taskName;
        private String taskInstanceName;
    }

    public static class ReportTaskStartedEvent extends ReportTaskEvent {
        public ReportTaskStartedEvent(String workflowName, String workflowInstanceName, String taskName, String taskInstanceName) {
            super(workflowName, workflowInstanceName, taskName, taskInstanceName);
        }
    }

    public static class ReportTaskCompletedEvent extends ReportTaskEvent {
        public ReportTaskCompletedEvent(String workflowName, String workflowInstanceName, String taskName, String taskInstanceName) {
            super(workflowName, workflowInstanceName, taskName, taskInstanceName);
        }
    }

    public static class ReportTaskTimeoutEvent extends ReportTaskEvent {
        public ReportTaskTimeoutEvent(String workflowName, String workflowInstanceName, String taskName, String taskInstanceName) {
            super(workflowName, workflowInstanceName, taskName, taskInstanceName);
        }
    }

    public static class ReportTaskFailedEvent extends ReportTaskEvent {
        public ReportTaskFailedEvent(String workflowName, String workflowInstanceName, String taskName, String taskInstanceName) {
            super(workflowName, workflowInstanceName, taskName, taskInstanceName);
        }
    }
    /*******************************************************************************/

}
