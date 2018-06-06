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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.beethoven.Beethoven;
import io.beethoven.api.HandlerResource;
import io.beethoven.api.MetricsResource;
import io.beethoven.api.TaskResource;
import io.beethoven.api.WorkflowResource;
import io.beethoven.repository.ContextualInputRepository;
import io.beethoven.repository.WorkflowRepository;
import io.beethoven.service.HandlerService;
import io.beethoven.service.TaskExecutorService;
import io.beethoven.service.TaskService;
import io.beethoven.service.WorkflowService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Davi Monteiro
 */
@Configuration
@ComponentScan
@ConditionalOnBean(BeethovenMarkerConfiguration.Marker.class)
@EnableConfigurationProperties(BeethovenProperties.class)
@Import(AkkaConfiguration.class)
public class BeethovenAutoConfiguration {

    @Bean
    @LoadBalanced
    @ConditionalOnMissingBean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        return mapper;
    }

    @Bean
    @ConditionalOnMissingBean
    public WorkflowResource workflowResource() {
        return new WorkflowResource();
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskResource taskResource() {
        return new TaskResource();
    }

    @Bean
    @ConditionalOnMissingBean
    public HandlerResource handlerResource() {
        return new HandlerResource();
    }

    @Bean
    @ConditionalOnMissingBean
    public MetricsResource metricsResource() {
        return new MetricsResource();
    }

    @Bean
    @ConditionalOnMissingBean
    public WorkflowRepository workflowRepository() throws Exception {
        WorkflowRepository workflowRepository = new WorkflowRepository();
        workflowRepository.saveAll(Beethoven.loadWorkflows());
        return workflowRepository;
    }

    @Bean
    @ConditionalOnMissingBean
    public ContextualInputRepository contextualInputRepository() {
        return new ContextualInputRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public HandlerService eventHandlerService() {
        return new HandlerService();
    }

    @Bean
    @ConditionalOnMissingBean
    public WorkflowService workflowService() {
        return new WorkflowService();
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskService taskService() {
        return new TaskService();
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskExecutorService taskExecutorService() {
        return new TaskExecutorService();
    }

    @Bean
    @Order
    public BeethovenContext beethovenContext() {
        return new BeethovenContext();
    }

}