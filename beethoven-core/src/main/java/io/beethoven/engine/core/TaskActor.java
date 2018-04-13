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
import io.beethoven.dsl.Task;
import io.beethoven.repository.WorkflowRepository;
import io.beethoven.service.TaskService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static akka.actor.ActorRef.noSender;

/**
 * @author Davi Monteiro
 */
@Component(ActorName.TASK_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TaskActor extends AbstractLoggingActor {

    @Autowired
    private TaskService taskService;

    @Override
    public Receive createReceive() {
        Receive receive = ReceiveBuilder.create()
                .match(StartTaskCommand.class, this::onStartTaskCommand)
                .build();

        return receive;
    }

    private void onStartTaskCommand(StartTaskCommand startTaskCommand) {
        log().debug("onStartTaskCommand: " + startTaskCommand);
        Task task = taskService.findByName(startTaskCommand.workflowName, startTaskCommand.taskName);
        taskService.execute(task, startTaskCommand.workflowInstanceName);
    }


    /**
     * ****************************************************************************
     * <p/>
     * Task Commands: START_TASK
     * <p/>
     * *****************************************************************************
     */
    @Data
    @AllArgsConstructor
    public static class StartTaskCommand {
        private String taskName;
        private String workflowName;
        private String workflowInstanceName;
    }
    /*******************************************************************************/

}
