/**
 * The MIT License
 * Copyright © 2018 Davi Monteiro
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.beethoven.repository;

import io.beethoven.dsl.ContextualInput;
import lombok.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Davi Monteiro
 */
@Repository
public class ContextualInputRepository {

    private Map<String, Set<ContextualInput>> globalInputs = new ConcurrentHashMap<>();
    private Map<String, Set<ContextualInput>> localInputs = new ConcurrentHashMap<>();

    public void saveGlobalInputs(@NonNull String workflowName, @NonNull Set<ContextualInput> inputs) {
        globalInputs.put(workflowName, inputs);
    }

    public Optional<ContextualInput> findGlobalContextualInput(@NonNull String workflowName, String key) {
        Set<ContextualInput> inputs = globalInputs.get(workflowName);

        Optional<ContextualInput> contextualInput = null;

        if (inputs != null && !inputs.isEmpty()) {
            contextualInput = inputs.stream().filter(input -> input.getKey().equals(key)).findFirst();
        }

        return contextualInput;
    }

    public void saveLocalInput(@NonNull String workflowInstanceName, @NonNull ContextualInput input) {
        Set<ContextualInput> inputs = localInputs.get(workflowInstanceName);

        if (inputs != null && !inputs.isEmpty()) {
            inputs.add(input);
        }
    }

    public Optional<ContextualInput> findLocalContextualInput(@NonNull String workflowInstanceName, @NonNull String key) {
        Set<ContextualInput> inputs = localInputs.get(workflowInstanceName);
        Optional<ContextualInput> contextualInput = null;

        if (inputs != null && !inputs.isEmpty()) {
            contextualInput = inputs.stream().filter(input -> input.getKey().equals(key)).findFirst();
        }

        return contextualInput;
    }

    public void deleteGlobalContextualInput(@NonNull String workflowName) {
        globalInputs.remove(workflowName);
    }

    public void deleteLocalContextualInput(@NonNull String workflowInstanceName) {
        localInputs.remove(workflowInstanceName);
    }

}
