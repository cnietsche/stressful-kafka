package com.stress.controller;

import com.stress.dto.CountResult;
import com.stress.dto.ProcessResult;
import com.stress.service.KafkaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProcessController {

    private final KafkaService kafkaService;

    public ProcessController(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @PostMapping("/process")
    public ResponseEntity<ProcessResult> process(@RequestParam(defaultValue = "1") int quantity)
            throws Exception {
        kafkaService.sendProcessRequests(quantity);
        return ResponseEntity.ok(new ProcessResult(quantity));
    }

    @GetMapping("/count")
    public ResponseEntity<CountResult> count() throws Exception {
        long count = kafkaService.getProcessedCount();
        return ResponseEntity.ok(new CountResult(count));
    }
}
