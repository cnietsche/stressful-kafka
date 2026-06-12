package com.stress.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stress.config.KafkaConfig;
import com.stress.dto.CountRequestMessage;
import com.stress.dto.CountResponseMessage;
import com.stress.dto.ProcessRequestMessage;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class KafkaService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaService(
            KafkaTemplate<String, String> kafkaTemplate,
            ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.replyingKafkaTemplate = replyingKafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendProcessRequests(int quantity) throws JsonProcessingException {
        for (int i = 0; i < quantity; i++) {
            ProcessRequestMessage message = new ProcessRequestMessage(UUID.randomUUID().toString());
            kafkaTemplate.send(
                    KafkaConfig.PROCESS_REQUEST_TOPIC,
                    objectMapper.writeValueAsString(message));
        }
    }

    public long getProcessedCount()
            throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        String correlationId = UUID.randomUUID().toString();
        CountRequestMessage request = new CountRequestMessage(correlationId, KafkaConfig.COUNT_RESPONSE_TOPIC);
        String payload = objectMapper.writeValueAsString(request);

        ProducerRecord<String, String> record = new ProducerRecord<>(KafkaConfig.COUNT_REQUEST_TOPIC, payload);
        record.headers().add(new RecordHeader(
                "kafka_correlationId",
                correlationId.getBytes(StandardCharsets.UTF_8)));

        var reply = replyingKafkaTemplate.sendAndReceive(record).get(5, TimeUnit.SECONDS);
        CountResponseMessage response = objectMapper.readValue(reply.value(), CountResponseMessage.class);
        return response.count();
    }
}
