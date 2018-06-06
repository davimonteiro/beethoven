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

import io.beethoven.dsl.Task;
import io.beethoven.dsl.Workflow;
import io.beethoven.repository.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @author Davi Monteiro
 */
@Service
public class TaskService {

    @Autowired
    private WorkflowRepository workflowRepository;

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

}
