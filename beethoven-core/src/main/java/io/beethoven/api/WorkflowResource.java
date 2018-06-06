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

import io.beethoven.api.dto.BeethovenOperation;
import io.beethoven.dsl.Workflow;
import io.beethoven.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

/**
 * @author Davi Monteiro
 */
@RestController
@RequestMapping(value = "api/workflows")
public class WorkflowResource {

    @Autowired
    private WorkflowService workflowService;


    //--------------------- POST methods -------------------- //

    @PostMapping
    public ResponseEntity createWorkflow(@RequestBody Workflow workflow) {
        Workflow createdWorkflow = workflowService.save(workflow);
        return ok(createdWorkflow);
    }

    @PostMapping(value = "/{workflowName}/operations")
    public ResponseEntity send(@PathVariable String workflowName, @RequestBody BeethovenOperation operation) {
        workflowService.execute(workflowName, operation);
        return ok().build();
    }

    //------------------------------------------------------------ //


    //------------------- GET methods ------------------ //

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity findAllWorflows() {
        List<Workflow> result = workflowService.findAll();
        return ok(result);
    }

    @GetMapping(value = "/{workflowName}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity findWorkflowByName(@PathVariable String workflowName) {
        Workflow workflow = workflowService.findByName(workflowName);
        return ok(workflow);
    }

    //------------------------------------------------------------ //


    //------------------- PUT methods ------------------ //

    @PutMapping(value = "/{workflowName}")
    public ResponseEntity updateWorkflow(@PathVariable String workflowName,
                                         @RequestBody Workflow workflow) {
        Workflow currentWorkflow = workflowService.update(workflowName, workflow);
        return ok(currentWorkflow);
    }

    //------------------------------------------------------------ //


    //------------------- DELETE methods ------------------ //

    @DeleteMapping(value = "/{workflowName}")
    public ResponseEntity deleteWorkflow(@PathVariable String workflowName) {
        workflowService.delete(workflowName);
        return noContent().build();
    }

    //------------------------------------------------------------ //

}
