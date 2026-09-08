package com.app.book.model;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record Book(long id, String title, String author) {}
