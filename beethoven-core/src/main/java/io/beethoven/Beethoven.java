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
package io.beethoven;

import akka.actor.ActorSystem;
import io.beethoven.config.SpringExtension;
import io.beethoven.dsl.Workflow;
import io.beethoven.engine.core.ActorName;
import io.beethoven.partitur.partitur.PartiturWorkflow;
import io.beethoven.dsl.PartiturWorkflowSerializer;
import org.apache.commons.io.FilenameUtils;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Davi Monteiro
 */
public class Beethoven {

    public static final String PATH_WORKFLOWS = "workflows/";
    public static final String PARTITUR_EXTENSION = "partitur";

    public static void initialize(ActorSystem actorSystem) {
        // Initialize the main actors
        actorSystem.actorOf(SpringExtension.SpringExtProvider.get(actorSystem).props(ActorName.WORKFLOW_ACTOR), ActorName.WORKFLOW_ACTOR);
        actorSystem.actorOf(SpringExtension.SpringExtProvider.get(actorSystem).props(ActorName.DECIDER_ACTOR), ActorName.DECIDER_ACTOR);
        actorSystem.actorOf(SpringExtension.SpringExtProvider.get(actorSystem).props(ActorName.TASK_ACTOR), ActorName.TASK_ACTOR);
        actorSystem.actorOf(SpringExtension.SpringExtProvider.get(actorSystem).props(ActorName.REPORTER_ACTOR), ActorName.REPORTER_ACTOR);
    }

    public static List<Workflow> loadWorkflows() throws Exception {
        List<Workflow> workflows = new ArrayList<>();

        Path workflowPath = Paths.get(ClassLoader.getSystemResource(PATH_WORKFLOWS).toURI());
        try (Stream<Path> paths = Files.walk(workflowPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> FilenameUtils.getExtension(path.getFileName().toString()).equals(PARTITUR_EXTENSION))
                    .forEach(path -> {

                        try (Reader reader = Files.newBufferedReader(path)) {
                            PartiturParser partiturParser = new PartiturParser();
                            PartiturWorkflow partiturWorkflow = partiturParser.parse(reader);
                            PartiturWorkflowSerializer serializer = new PartiturWorkflowSerializer();
                            Workflow workflow = serializer.to(partiturWorkflow);
                            System.out.println(workflow);
                            workflows.add(workflow);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }

        return workflows;
    }


}
