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
package io.beethoven.api;

import io.beethoven.dsl.Task;
import io.beethoven.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping(value = "api/workflows/{workflowName}/tasks")
public class TaskResource {

    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity createTask(@PathVariable String workflowName,
                                     @RequestBody Task task) {
        Task createdTask = taskService.save(workflowName, task);
        return ok(createdTask);
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity findAllTasks(@PathVariable String workflowName) {
        Set<Task> tasks = taskService.findAll(workflowName);
        return ok(tasks);
    }

    @GetMapping(value = "/{taskName}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity findTaskByName(@PathVariable String workflowName,
                                         @PathVariable String taskName) {
        Task task = taskService.findByName(workflowName, taskName);
        return ok(task);
    }

    @PutMapping(value = "/{taskName}")
    public ResponseEntity updateTask(@PathVariable String workflowName,
                                     @PathVariable String taskName,
                                     @RequestBody Task task) throws Exception {
        Task currentTask = taskService.update(workflowName, taskName, task);
        return ok(currentTask);
    }

    @DeleteMapping(value = "/{taskName}")
    public ResponseEntity deleteTask(@PathVariable String workflowName,
                                     @PathVariable String taskName) {
        taskService.delete(workflowName, taskName);
        return noContent().build();
    }

}
