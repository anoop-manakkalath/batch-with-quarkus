package com.example.batch.reader;

import java.io.BufferedReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.example.batch.model.Book;

import jakarta.batch.api.chunk.AbstractItemReader;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Named;

@Named("csvItemReader")
@Dependent
public class CsvItemReader extends AbstractItemReader {

	private BufferedReader reader;

    @Override
    public void open(Serializable checkpoint) throws Exception {
        var path = Paths.get("data/large_books.csv");
        this.reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
        reader.readLine(); // Skips header
    }

    @Override
    public Object readItem() throws Exception {
        var line = reader.readLine();
        if (Objects.isNull(line)) {
            return null; // Signals end of batch job to JBeret engine
        }
        var tokens = line.split(",");
        return new Book(
        		NumberUtils.toLong(StringUtils.trim(tokens[0])),
        		StringUtils.trim(tokens[1]),
        		StringUtils.trim(tokens[2])
        );
    }

    @Override
    public void close() throws Exception {
        if (Objects.isNull(reader)) {
            reader.close();
        }
    }
}
