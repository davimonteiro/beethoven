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
package io.beethoven.repository;

import io.beethoven.engine.TaskInstance;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Davi Monteiro
 */
@Repository
public class WorkflowInstanceRepository {

    private Map<String, Map<String, TaskInstance>> instances = new ConcurrentHashMap<>();

    public void save(String workflowInstance, TaskInstance taskInstance) {
        if (!contains(workflowInstance)) {
            instances.put(workflowInstance, new ConcurrentHashMap<>());
        }
        String contexInput = "${" + taskInstance.getTaskName() + ".response}";
        instances.get(workflowInstance).put(contexInput, taskInstance);
    }

    private boolean contains(String workflowInstance) {
        return instances.containsKey(workflowInstance);
    }

    public TaskInstance findTaskInstanceByName(String workflowInstance, String contexInput) {
        return instances.get(workflowInstance).get(contexInput);
    }

}
