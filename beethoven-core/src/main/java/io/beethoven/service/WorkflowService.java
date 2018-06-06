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
package io.beethoven.service;

import akka.actor.ActorSystem;
import io.beethoven.api.dto.BeethovenOperation;
import io.beethoven.api.dto.BeethovenOperation.Operation;
import io.beethoven.dsl.Workflow;
import io.beethoven.engine.core.WorkflowActor.CancelWorkflowCommand;
import io.beethoven.engine.core.WorkflowActor.ScheduleWorkflowCommand;
import io.beethoven.engine.core.WorkflowActor.StartWorkflowCommand;
import io.beethoven.engine.core.WorkflowActor.StopWorkflowCommand;
import io.beethoven.repository.ContextualInputRepository;
import io.beethoven.repository.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static akka.actor.ActorRef.noSender;
import static io.beethoven.engine.core.ActorPath.WORKFLOW_ACTOR;

/**
 * @author Davi Monteiro
 */
@Service
public class WorkflowService {

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private ContextualInputRepository contextualInputRepository;

    @Autowired
    private ActorSystem actorSystem;

    public void execute(String workflowName, BeethovenOperation operation) {
        contextualInputRepository.saveGlobalInputs(workflowName, operation.getInputs());
        switch (Operation.findById(operation.getOperation())) {
            case SCHEDULE:
                actorSystem.actorSelection(WORKFLOW_ACTOR)
                        .tell(new ScheduleWorkflowCommand(workflowName), noSender());
                break;
            case START:
                System.out.println();
                actorSystem.actorSelection(WORKFLOW_ACTOR)
                        .tell(new StartWorkflowCommand(workflowName, operation.getInstanceName()), noSender());
                break;
            case STOP:
                actorSystem.actorSelection(WORKFLOW_ACTOR)
                        .tell(new StopWorkflowCommand(workflowName, operation.getInstanceName()), noSender());
                break;
            case CANCEL:
                actorSystem.actorSelection(WORKFLOW_ACTOR)
                        .tell(new CancelWorkflowCommand(workflowName, operation.getInstanceName()), noSender());
                break;
        }
    }

    public Workflow save(Workflow workflow) {
        workflowRepository.save(workflow);
        return workflow;
    }

    public List<Workflow> findAll() {
        return workflowRepository.findAll();
    }

    public Workflow findByName(String workflowName) {
        return workflowRepository.findByName(workflowName);
    }

    public Workflow update(String workflowName, Workflow workflow) {
        return save(workflow);
    }

    public void delete(String workflowName) {
        workflowRepository.delete(workflowName);
    }

}
