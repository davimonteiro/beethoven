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

import io.beethoven.dsl.Handler;
import io.beethoven.dsl.Workflow;
import io.beethoven.repository.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.beethoven.dsl.Handler.EventType;
import static org.springframework.beans.BeanUtils.copyProperties;

/**
 * @author Davi Monteiro
 */
@Service
public class HandlerService {

    @Autowired
    private WorkflowRepository workflowRepository;

    public List<Handler> find(String workflowName, EventType eventType) {
        Workflow workflow = workflowRepository.findByName(workflowName);
        List<Handler> handlers = workflow.getHandlers().stream()
                .filter(handler -> handler.getEventType().equals(eventType))
                .collect(Collectors.toList());
        return handlers;
    }

    public Handler save(String workflowName, Handler handler) {
        Workflow workflow = workflowRepository.findByName(workflowName);
        return workflow.createHandler(handler);
    }

    public Set<Handler> findAll(String workflowName) {
        Workflow workflow = workflowRepository.findByName(workflowName);
        return workflow.getHandlers();
    }

    public Handler findByName(String workflowName, String handlerName) {
        Workflow workflow = workflowRepository.findByName(workflowName);
        return workflow.findHandlerByName(handlerName);
    }

    public Handler update(String workflowName, String handlerName, Handler handler) {
        Workflow workflow = workflowRepository.findByName(workflowName);
        return workflow.createHandler(handler);
    }

    public void delete(String workflowName, String handlerName) {
        Workflow workflow = workflowRepository.findByName(workflowName);
        workflow.getHandlers().removeIf(handler -> handler.getName().equals(handlerName));
    }

}
