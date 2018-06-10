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

import io.beethoven.dsl.Workflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

/**
 * @author Davi Monteiro
 */
@Repository
public class WorkflowRepository {

    private Map<String, Workflow> workflows = new ConcurrentHashMap<>();

    @Autowired
    private ContextualInputRepository contextualInputRepository;

    public void save(Workflow workflow) {
        requireNonNull(workflow);
        workflows.put(workflow.getName(), workflow);
    }

    public void saveAll(List<Workflow> workflows) {
        requireNonNull(workflows);
        workflows.forEach(this::save);
    }

    public Workflow findByName(String name) {
        requireNonNull(name);
        return workflows.get(name);
    }

    public List<Workflow> findAll() {
        return new ArrayList<>(workflows.values());
    }

    public void delete(String name) {
        requireNonNull(name);
        workflows.remove(name);
        contextualInputRepository.deleteGlobalContextualInput(name);
    }

}
