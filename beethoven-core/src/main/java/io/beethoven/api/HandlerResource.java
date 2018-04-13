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

import io.beethoven.dsl.Handler;
import io.beethoven.service.HandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

/**
 * @author Davi Monteiro
 */
@RestController
@RequestMapping(value = "api/workflows/{workflowName}/handlers")
public class HandlerResource {

    @Autowired
    private HandlerService handlerService;

    @PostMapping
    public ResponseEntity createHandler(@PathVariable String workflowName,
                                        @RequestBody Handler handler) {
        Handler createdHandler = handlerService.save(workflowName, handler);
        return ok(createdHandler);
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity findAllHandlers(@PathVariable String workflowName) {
        Set<Handler> handlers = handlerService.findAll(workflowName);
        return ok(handlers);
    }

    @GetMapping(value = "/{handlerName}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity findHandlerByName(@PathVariable String workflowName,
                                            @PathVariable String handlerName) {
        Handler handler = handlerService.findByName(workflowName, handlerName);
        return ok(handler);
    }

    @PutMapping(value = "/{handlerName}")
    public ResponseEntity updateHandler(@PathVariable String workflowName,
                                        @PathVariable String handlerName,
                                        @RequestBody Handler handler) {
        Handler currentHandler = handlerService.update(workflowName, handlerName, handler);
        return ok(currentHandler);
    }

    @DeleteMapping(value = "/{handlerName}")
    public ResponseEntity deleteHandler(@PathVariable String workflowName,
                                        @PathVariable String handlerName) {
        handlerService.delete(workflowName, handlerName);
        return noContent().build();
    }

}
