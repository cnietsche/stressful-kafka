package com.stress.dto;

public record CountRequestMessage(String correlationId, String replyTopic) {
}
