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

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import io.beethoven.dsl.*;
import io.beethoven.engine.TaskInstance;
import io.beethoven.engine.core.DeciderActor.TaskCompletedEvent;
import io.beethoven.engine.core.DeciderActor.TaskFailedEvent;
import io.beethoven.repository.WorkflowInstanceRepository;
import io.beethoven.repository.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static io.beethoven.engine.core.ActorPath.DECIDER_ACTOR;
import static io.beethoven.engine.core.DeciderActor.TaskEvent;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;

/**
 * @author Davi Monteiro
 */
@Service
public class TaskService {

    private final Pattern pattern = Pattern.compile("\\$\\{(.)*}");

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private ActorSystem actorSystem;


    public Task save(String workflowName, Task task) {
        Workflow workflow = workflowRepository.findByName(workflowName);
        return workflow.createTask(task);
    }

    public Set<Task> findAll(String workflowName) {
        Workflow workflow = workflowRepository.findByName(workflowName);
        return workflow.getTasks();
    }

    public Task findByName(String workflowName, String taskName) {
        Workflow workflow = workflowRepository.findByName(workflowName);
        return workflow.findTaskByName(taskName);
    }

    public Task update(String workflowName, String taskName, Task task) {
        return save(workflowName, task);
    }

    public void delete(String workflowName, String taskName) {
        Workflow workflow = workflowRepository.findByName(workflowName);
        workflow.getTasks().removeIf(task -> task.getName().equals(taskName));
    }


    public void execute(Task task, String workflowInstanceName) {
        TaskInstance taskInstance = buildTaskInstance(task);

        // Prepare a http request
        RequestHeadersSpec request = prepareHttpRequest(task.getHttpRequest(), workflowInstanceName);

        // Peform the request
        request.retrieve().bodyToFlux(String.class)
                .subscribe(response -> {
                    taskInstance.setResponse(response);
                    workflowInstanceRepository.save(workflowInstanceName, taskInstance);
                    sendEvent(new TaskCompletedEvent(task.getName(), workflowInstanceName, task.getWorkflowName()));
                }, throwable -> {
                    taskInstance.setFailure(throwable);
                    workflowInstanceRepository.save(workflowInstanceName, taskInstance);
                    sendEvent(new TaskFailedEvent(task.getName(), taskInstance.getTaskInstanceName(), task.getWorkflowName()));
                });
    }

    private RequestHeadersSpec prepareHttpRequest(HttpRequest httpRequest, String workflowInstanceName) {
        RequestHeadersSpec request = null;

        switch (httpRequest.getMethod()) {
            case GET:
                request = buildGetRequest(httpRequest);
                break;
            case POST:
                request = buildPostRequest(httpRequest, workflowInstanceName);
                break;
            case PUT:
                request = buildPutRequest(httpRequest);
                break;
            case DELETE:
                request = buildDeleteRequest(httpRequest);
                break;
        }

        return request;
    }

    private boolean isContextualInput(String input) {
        return pattern.matcher(input).matches();
    }

    private RequestHeadersSpec buildGetRequest(HttpRequest httpRequest) {
        RequestHeadersSpec request = webClientBuilder.build().get()
                .uri(httpRequest.getUrl(), httpRequest.getUriVariables());
        addHeaders(request, httpRequest.getHeaders());
        addQueryParams(request, httpRequest.getParams());

        return request;
    }

    private RequestHeadersSpec buildPostRequest(HttpRequest httpRequest, String workflowInstanceName) {
        String body = httpRequest.getBody();
        if (isContextualInput(body)) {
            TaskInstance taskInstance = workflowInstanceRepository.findTaskInstanceByName(workflowInstanceName, body);
            body = taskInstance.getResponse();
        }

        RequestHeadersSpec request = webClientBuilder.build().post()
                .uri(httpRequest.getUrl(), httpRequest.getUriVariables())
                .body(BodyInserters.fromPublisher(Flux.just(body), String.class));
        addHeaders(request, httpRequest.getHeaders());
        addQueryParams(request, httpRequest.getParams());

        return request;
    }

    private RequestHeadersSpec buildPutRequest(HttpRequest httpRequest) {
        RequestHeadersSpec request = webClientBuilder.build().put()
                .uri(httpRequest.getUrl(), httpRequest.getUriVariables())
                .body(fromObject(httpRequest.getBody()));
        addHeaders(request, httpRequest.getHeaders());
        addQueryParams(request, httpRequest.getParams());

        return request;
    }

    private RequestHeadersSpec buildDeleteRequest(HttpRequest httpRequest) {
        RequestHeadersSpec request = webClientBuilder.build().delete()
                .uri(httpRequest.getUrl(), httpRequest.getUriVariables());
        addHeaders(request, httpRequest.getHeaders());
        addQueryParams(request, httpRequest.getParams());

        return request;
    }

    private void addHeaders(RequestHeadersSpec request, List<Header> headers) {
        for (Header header : headers) {
            request.header(header.getName(), header.getValue());
        }
    }

    private void addQueryParams(RequestHeadersSpec request, List<Param> params) {
        for (Param param : params) {
            request.attribute(param.getName(), param.getValue());
        }
    }

    private TaskInstance buildTaskInstance(Task task) {
        String taskInstanceName = UUID.randomUUID().toString();
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskName(task.getName());
        taskInstance.setTaskInstanceName(taskInstanceName);
        return taskInstance;
    }

    private void sendEvent(TaskEvent taskEvent) {
        actorSystem.actorSelection(DECIDER_ACTOR).tell(taskEvent, ActorRef.noSender());
    }

}
