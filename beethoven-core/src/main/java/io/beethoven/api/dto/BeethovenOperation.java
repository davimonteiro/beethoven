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
package io.beethoven.api.dto;

import io.beethoven.dsl.ContextualInput;
import lombok.Data;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.util.Optional.ofNullable;

@Data
public class BeethovenOperation {

    private String workflowName;
    private String instanceName;
    private Operation operation;
    private Set<ContextualInput> inputs = new HashSet<>();

    public void setOperation(String operation) {
        this.operation = Operation.findById(operation);
    }

    public String getOperation() {
        return ofNullable(operation)
                .map(op -> op.id)
                .orElseGet(null);
    }

    public enum Operation {
        SCHEDULE("schedule"),
        START("start"),
        STOP("stop"),
        CANCEL("cancel");

        @Getter
        private String id;

        Operation(String id) {
            this.id = id;
        }

        public static Operation findById(String id) {
            return Arrays.stream(values())
                    .filter(op -> op.id.equals(id)).findFirst()
                    .orElseGet(null);
        }

    }
}
