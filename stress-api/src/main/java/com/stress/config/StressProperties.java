package com.stress.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stress")
public class StressProperties {

    private long processingDelayMs = 1000;

    public long getProcessingDelayMs() {
        return processingDelayMs;
    }

    public void setProcessingDelayMs(long processingDelayMs) {
        this.processingDelayMs = processingDelayMs;
    }
}
