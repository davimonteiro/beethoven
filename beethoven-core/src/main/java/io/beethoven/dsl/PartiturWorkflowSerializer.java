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

import io.beethoven.partitur.partitur.*;
import org.eclipse.emf.ecore.EObject;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.beethoven.dsl.Command.CommandFactory.createCommand;
import static io.beethoven.dsl.ConditionFunctionFactory.createCondition;
import static java.util.Objects.nonNull;

/**
 * @author Davi Monteiro
 */
public class PartiturWorkflowSerializer {

    public Workflow to(PartiturWorkflow partiturWorkflow) {
        Workflow workflow = new Workflow();
        workflow.setName(partiturWorkflow.getName());
        workflow.setTasks(convertToTasks(partiturWorkflow.getTasks(), partiturWorkflow.getName()));
        workflow.setHandlers(convertToHandlers(partiturWorkflow.getHandlers()));
        return workflow;
    }

    private Set<Task> convertToTasks(List<PartiturTask> partiturTasks, String workflowName) {
        Set<Task> tasks = new HashSet<>();

        for (PartiturTask partiturTask : partiturTasks) {

            String taskName = partiturTask.getName();
            HttpMethod method = null;
            String url = null;
            List<Header> headers = null;
            List<Param> params = new ArrayList<>();
            List<String> uriVariables = null;
            String body = null;

            EObject request = partiturTask.getPartiturHttpRequest();

            if (request instanceof HttpGet) {
                method = HttpMethod.GET;
                HttpGet httpGet = (HttpGet) request;
                url = httpGet.getUrl();
                uriVariables = convertToUriVariables(httpGet.getUriVariables());
                headers = convertToHeaders(httpGet.getHeaders());
                params = convertToQueryParams(httpGet.getParams());
            } else if (request instanceof HttpPost) {
                method = HttpMethod.POST;
                HttpPost httpPost = (HttpPost) request;
                url = httpPost.getUrl();
                uriVariables = convertToUriVariables(httpPost.getUriVariables());
                headers = convertToHeaders(httpPost.getHeaders());
                body = convertToBody(httpPost.getBody());
            } else if (request instanceof HttpPut) {
                method = HttpMethod.PUT;
                HttpPut httpPut = (HttpPut) request;
                url = httpPut.getUrl();
                uriVariables = convertToUriVariables(httpPut.getUriVariables());
                headers = convertToHeaders(httpPut.getHeaders());
                body = convertToBody(httpPut.getBody());
            } else if (request instanceof HttpDelete) {
                method = HttpMethod.DELETE;
                HttpDelete httpDelete = (HttpDelete) request;
                url = httpDelete.getUrl();
                uriVariables = convertToUriVariables(httpDelete.getUriVariables());
                headers = convertToHeaders(httpDelete.getHeaders());
            }

            HttpRequest httpRequest = new HttpRequest();
            httpRequest.setMethod(method);
            httpRequest.setUrl(url);
            httpRequest.setUriVariables(uriVariables);
            httpRequest.setHeaders(headers);
            httpRequest.setParams(params);
            httpRequest.setBody(body);

            Task task = new Task();
            task.setName(taskName);
            task.setHttpRequest(httpRequest);
            task.setWorkflowName(workflowName);
            tasks.add(task);
        }

        return tasks;
    }

    private String convertToBody(HttpBody body) {
        String result = "";
        if (nonNull(body)) {
            result = body.getValue();
        }
        return result;
    }

    private List<String> convertToUriVariables(UriVariables uriVariables) {
        List<String> variables = new ArrayList<>();
        if (nonNull(uriVariables)) {
            variables.addAll(uriVariables.getValues());
        }
        return variables;
    }


    private List<Param> convertToQueryParams(List<QueryParam> queryParams) {
        List<Param> params = new ArrayList<>();

        for (QueryParam queryParam : queryParams) {
            Param param = new Param();
            param.setName(queryParam.getName());
            param.setValue(queryParam.getValue());

            params.add(param);
        }

        return params;
    }

    private List<Header> convertToHeaders(List<HttpHeader> httpHeaders) {
        List<Header> headers = new ArrayList<>();

        for (HttpHeader httpHeader : httpHeaders) {
            Header header = new Header();
            header.setName(httpHeader.getName());
            header.setValue(httpHeader.getValue());

            headers.add(header);
        }

        return headers;
    }

    private Set<Handler> convertToHandlers(List<PartiturHandler> partiturHandlers) {
        Set<Handler> handlers = new HashSet<>();

        for (PartiturHandler partiturHandler : partiturHandlers) {

            String eventName = partiturHandler.getEvent().getName();
            Handler.EventType eventType = Handler.EventType.valueOf(eventName);

            Handler handler = new Handler();
            handler.setName(partiturHandler.getName());
            handler.setEventType(eventType);
            handler.setConditions(convertToConditions(partiturHandler.getConditions()));
            handler.setCommands(convertToCommands(partiturHandler.getCommands()));

            handlers.add(handler);
        }

        return handlers;
    }

    private List<Condition> convertToConditions(List<PartiturCondition> partiturConditions) {
        List<Condition> conditions = new ArrayList<>();

        for (PartiturCondition partiturCondition : partiturConditions) {
            String function = partiturCondition.getConditionFunction().getName();
            String arg = partiturCondition.getArg();

            conditions.add(createCondition(function, arg));
        }

        return conditions;
    }

    private List<Command> convertToCommands(List<PartiturCommand> partiturCommands) {
        List<Command> commands = new ArrayList<>();

        for (PartiturCommand partiturCommand : partiturCommands) {
            String commandFunction = partiturCommand.getCommandFunction().getName();
            String arg = partiturCommand.getArg();

            commands.add(createCommand(commandFunction, arg));
        }

        return commands;
    }

}
