package com.stress.listener;

import com.stress.config.StressProperties;
import com.stress.entity.Dummy;
import com.stress.repository.DummyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProcessListener {

    private static final Logger log = LoggerFactory.getLogger(ProcessListener.class);

    private final DummyRepository dummyRepository;
    private final StressProperties stressProperties;

    public ProcessListener(DummyRepository dummyRepository, StressProperties stressProperties) {
        this.dummyRepository = dummyRepository;
        this.stressProperties = stressProperties;
    }

    @KafkaListener(topics = "process-request", groupId = "stress-process-group", concurrency = "1")
    public void onProcessRequest(String message) throws InterruptedException {
        log.debug("Processing request: {}", message);
        Thread.sleep(stressProperties.getProcessingDelayMs());
        dummyRepository.save(new Dummy(true));
    }
}
