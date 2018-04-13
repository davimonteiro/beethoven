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
package io.beethoven.config;

import akka.actor.ActorSystem;
import io.beethoven.engine.core.DeciderActor;
import io.beethoven.engine.core.ReporterActor;
import io.beethoven.engine.core.TaskActor;
import io.beethoven.engine.core.WorkflowActor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import static io.beethoven.engine.core.ActorName.ACTOR_SYSTEM;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * A configuration class to create Spring managed Actor Beans and the Actor System.
 *
 * @author Davi Monteiro
 */
@Configuration
public class AkkaConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public ActorSystem actorSystem() {
        ActorSystem actorSystem = ActorSystem.create(ACTOR_SYSTEM);
        // Initialize the application context in the Akka Spring Extension
        SpringExtension.SpringExtProvider.get(actorSystem).initialize(applicationContext);
        return actorSystem;
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public WorkflowActor workflowActor() {
        return new WorkflowActor();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public DeciderActor deciderActor() {
        return new DeciderActor();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public TaskActor taskActor() {
        return new TaskActor();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public ReporterActor reporterActor() {
        return new ReporterActor();
    }

}
