package com.example.batch.model;

public record JobExceptionStatusResponse (
        long executionId,
    String name,
    String batchStatus,
    String exception
) {}
