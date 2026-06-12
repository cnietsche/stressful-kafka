package com.stress.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stress.dto.CountRequestMessage;
import com.stress.dto.CountResponseMessage;
import com.stress.repository.DummyRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class CountListener {

    private static final Logger log = LoggerFactory.getLogger(CountListener.class);

    private final DummyRepository dummyRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public CountListener(
            DummyRepository dummyRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.dummyRepository = dummyRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "count-request", groupId = "stress-count-group", concurrency = "1")
    public void onCountRequest(String message) throws JsonProcessingException {
        CountRequestMessage request = objectMapper.readValue(message, CountRequestMessage.class);
        long count = dummyRepository.countByProcessadoTrue();

        CountResponseMessage response = new CountResponseMessage(request.correlationId(), count);
        String payload = objectMapper.writeValueAsString(response);

        ProducerRecord<String, String> record = new ProducerRecord<>(request.replyTopic(), payload);
        record.headers().add(new RecordHeader(
                "kafka_correlationId",
                request.correlationId().getBytes(StandardCharsets.UTF_8)));

        kafkaTemplate.send(record);
        log.debug("Count response sent: correlationId={}, count={}", request.correlationId(), count);
    }
}
