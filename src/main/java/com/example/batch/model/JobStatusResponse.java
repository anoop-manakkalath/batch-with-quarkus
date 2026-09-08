package com.example.batch.model;

public record JobStatusResponse(
    long executionId,
    String name,
    String batchStatus
) {}
