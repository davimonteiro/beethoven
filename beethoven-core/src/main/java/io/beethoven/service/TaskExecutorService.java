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
import io.beethoven.dsl.*;
import io.beethoven.engine.TaskInstance;
import io.beethoven.engine.core.DeciderActor;
import io.beethoven.repository.ContextualInputRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static akka.actor.ActorRef.noSender;
import static io.beethoven.engine.core.ActorPath.DECIDER_ACTOR;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;

/**
 * @author Davi Monteiro
 */
@Service
public class TaskExecutorService {

    @Autowired
    private ContextualInputRepository contextualInputRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private ActorSystem actorSystem;

    public void execute(Task task, String workflowInstanceName) {
        TaskInstance taskInstance = buildTaskInstance(task);

        // Prepare a http request
        WebClient.RequestHeadersSpec request = buildHttpRequest(task.getHttpRequest(), task.getWorkflowName(), workflowInstanceName);

        // Peform the request
        request.retrieve().bodyToFlux(String.class)
                .subscribe(
                        response -> handleSuccessResponse(task, workflowInstanceName, taskInstance, response),
                        throwable -> handleFailureResponse(task, workflowInstanceName, taskInstance, throwable));
    }

    private void handleSuccessResponse(Task task, String workflowInstanceName, TaskInstance taskInstance, String response) {
        taskInstance.setResponse(response);
        contextualInputRepository.saveLocalInput(workflowInstanceName, buildContextualInput(taskInstance));
        sendEvent(new DeciderActor.TaskCompletedEvent(task.getName(), workflowInstanceName, task.getWorkflowName()));
    }

    private ContextualInput buildContextualInput(TaskInstance taskInstance) {
        String contexInputKey = "${" + taskInstance.getTaskName() + ".response}";
        return new ContextualInput(contexInputKey, taskInstance.getResponse());
    }

    private void handleFailureResponse(Task task, String workflowInstanceName, TaskInstance taskInstance, Throwable throwable) {
        taskInstance.setFailure(throwable);
        contextualInputRepository.saveLocalInput(workflowInstanceName, buildContextualInput(taskInstance));
        sendEvent(new DeciderActor.TaskFailedEvent(task.getName(), taskInstance.getTaskInstanceName(), task.getWorkflowName()));
    }

    private WebClient.RequestHeadersSpec buildHttpRequest(HttpRequest httpRequest, String workflowName, String workflowInstanceName) {

        WebClient.RequestHeadersSpec request = null;

        switch (httpRequest.getMethod()) {
            case GET:
                request = buildGetRequest(httpRequest, workflowName, workflowInstanceName);
                break;
            case POST:
                request = buildPostRequest(httpRequest, workflowName, workflowInstanceName);
                break;
            case PUT:
                request = buildPutRequest(httpRequest, workflowName, workflowInstanceName);
                break;
            case DELETE:
                request = buildDeleteRequest(httpRequest, workflowName, workflowInstanceName);
                break;
        }

        return request;
    }

    private WebClient.RequestHeadersSpec buildGetRequest(HttpRequest httpRequest, String workflowName, String workflowInstanceName) {
        List<String> uriVariables = buildUriVariables(httpRequest.getUriVariables(), workflowName, workflowInstanceName);

        WebClient.RequestHeadersSpec request = webClientBuilder.build().get()
                .uri(httpRequest.getUrl(), uriVariables);
        buildHeaders(request, httpRequest.getHeaders(), workflowName, workflowInstanceName);
        buildQueryParams(request, httpRequest.getParams(), workflowName, workflowInstanceName);

        return request;
    }

    private WebClient.RequestHeadersSpec buildPostRequest(HttpRequest httpRequest, String workflowName, String workflowInstanceName) {
        List<String> uriVariables = buildUriVariables(httpRequest.getUriVariables(), workflowName, workflowInstanceName);
        String body = buildBody(httpRequest.getBody(), workflowName, workflowInstanceName);

        WebClient.RequestHeadersSpec request = webClientBuilder.build().post()
                .uri(httpRequest.getUrl(), uriVariables)
                .body(BodyInserters.fromPublisher(Flux.just(body), String.class));
        buildHeaders(request, httpRequest.getHeaders(), workflowName, workflowInstanceName);
        buildQueryParams(request, httpRequest.getParams(), workflowName, workflowInstanceName);

        return request;
    }

    private WebClient.RequestHeadersSpec buildPutRequest(HttpRequest httpRequest, String workflowName, String workflowInstanceName) {
        List<String> uriVariables = buildUriVariables(httpRequest.getUriVariables(), workflowName, workflowInstanceName);
        String body = buildBody(httpRequest.getBody(), workflowName, workflowInstanceName);

        WebClient.RequestHeadersSpec request = webClientBuilder.build().put()
                .uri(httpRequest.getUrl(), uriVariables)
                .body(fromObject(body));
        buildHeaders(request, httpRequest.getHeaders(), workflowName, workflowInstanceName);
        buildQueryParams(request, httpRequest.getParams(), workflowName, workflowInstanceName);

        return request;
    }

    private WebClient.RequestHeadersSpec buildDeleteRequest(HttpRequest httpRequest, String workflowName, String workflowInstanceName) {
        List<String> uriVariables = buildUriVariables(httpRequest.getUriVariables(), workflowName, workflowInstanceName);

        WebClient.RequestHeadersSpec request = webClientBuilder.build().delete()
                .uri(httpRequest.getUrl(), uriVariables);
        buildHeaders(request, httpRequest.getHeaders(), workflowName, workflowInstanceName);
        buildQueryParams(request, httpRequest.getParams(), workflowName, workflowInstanceName);

        return request;
    }

    private String buildBody(String body, String workflowName, String workflowInstanceName) {
        return contextualInputRepository.findGlobalContextualInput(workflowName, body)
                .map(ContextualInput::getValue)
                .orElseGet(
                        () -> contextualInputRepository.findLocalContextualInput(workflowInstanceName, body)
                                .map(ContextualInput::getValue)
                                .orElse(body));
    }

    private List<String> buildUriVariables(List<String> uriVariables, String workflowName, String workflowInstanceName) {
        List<String> newVariables = new ArrayList<>();
        for (String uriVariable : uriVariables) {
            contextualInputRepository.findGlobalContextualInput(workflowName, uriVariable)
                    .ifPresent(contextualInput -> newVariables.add(contextualInput.getValue()));

            contextualInputRepository.findLocalContextualInput(workflowInstanceName, uriVariable)
                    .ifPresent(contextualInput -> newVariables.add(contextualInput.getValue()));
        }
        return newVariables;
    }

    private void buildHeaders(WebClient.RequestHeadersSpec request, List<Header> headers, String workflowName, String workflowInstanceName) {
        for (Header header : headers) {
            request.header(header.getName(), header.getValue());

            contextualInputRepository.findGlobalContextualInput(workflowName, header.getValue())
                    .ifPresent(contextualInput -> request.header(header.getName(), contextualInput.getValue()));

            contextualInputRepository.findLocalContextualInput(workflowInstanceName, header.getValue())
                    .ifPresent(contextualInput -> request.header(header.getName(), contextualInput.getValue()));
        }
    }

    private void buildQueryParams(WebClient.RequestHeadersSpec request, List<Param> params, String workflowName, String workflowInstanceName) {
        for (Param param : params) {
            request.attribute(param.getName(), param.getValue());

            contextualInputRepository.findGlobalContextualInput(workflowName, param.getValue())
                    .ifPresent(contextualInput -> request.header(param.getName(), contextualInput.getValue()));

            contextualInputRepository.findLocalContextualInput(workflowInstanceName, param.getValue())
                    .ifPresent(contextualInput -> request.header(param.getName(), contextualInput.getValue()));
        }
    }

    private TaskInstance buildTaskInstance(Task task) {
        String taskInstanceName = UUID.randomUUID().toString();
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskName(task.getName());
        taskInstance.setTaskInstanceName(taskInstanceName);
        return taskInstance;
    }

    private void sendEvent(DeciderActor.TaskEvent taskEvent) {
        actorSystem.actorSelection(DECIDER_ACTOR).tell(taskEvent, noSender());
    }

}
