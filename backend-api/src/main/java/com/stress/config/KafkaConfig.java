package com.stress.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class KafkaConfig {

    public static final String COUNT_REQUEST_TOPIC = "count-request";
    public static final String COUNT_RESPONSE_TOPIC = "count-response";
    public static final String PROCESS_REQUEST_TOPIC = "process-request";

    @Bean
    NewTopic processRequestTopic() {
        return TopicBuilder.name(PROCESS_REQUEST_TOPIC).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic countRequestTopic() {
        return TopicBuilder.name(COUNT_REQUEST_TOPIC).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic countResponseTopic() {
        return TopicBuilder.name(COUNT_RESPONSE_TOPIC).partitions(1).replicas(1).build();
    }

    @Bean
    ConcurrentMessageListenerContainer<String, String> replyContainer(
            ConsumerFactory<String, String> consumerFactory) {
        ContainerProperties containerProperties = new ContainerProperties(COUNT_RESPONSE_TOPIC);
        containerProperties.setGroupId("backend-api-reply-" + UUID.randomUUID());

        ConcurrentMessageListenerContainer<String, String> container =
                new ConcurrentMessageListenerContainer<>(consumerFactory, containerProperties);
        container.getContainerProperties().setMissingTopicsFatal(false);
        return container;
    }

    @Bean
    ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate(
            ProducerFactory<String, String> producerFactory,
            ConcurrentMessageListenerContainer<String, String> replyContainer) {
        ReplyingKafkaTemplate<String, String, String> template =
                new ReplyingKafkaTemplate<>(producerFactory, replyContainer);
        template.setDefaultReplyTimeout(Duration.ofSeconds(5));
        replyContainer.start();
        return template;
    }
}
