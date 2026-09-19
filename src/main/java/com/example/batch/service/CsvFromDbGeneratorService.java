package com.example.batch.service;

import com.example.batch.entity.BookEntity;
import com.example.batch.repository.BookRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.jbosslog.JBossLog;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

@ApplicationScoped
@JBossLog
public class CsvFromDbGeneratorService {

    private final BookRepository bookRepository;

    @Inject
    public CsvFromDbGeneratorService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Reads all existing records from DB and generates a CSV file of target MB size
     * by duplicating rows, assigning fresh IDs starting after max(id).
     *
     * @param outputPath Path where the generated CSV will be saved
     * @param maxLineNumber   Target max lines (e.g., 1000)
     */
    @SneakyThrows
    public void generateCsvFromDatabaseForLine(String outputPath, int maxLineNumber) {
        // 1. Fetch existing records from DB
        var existingBooks = bookRepository.findAllBooks();
        if (existingBooks.isEmpty()) {
            throw new IllegalStateException("No records found in database.");
        }
        var inTime = System.currentTimeMillis();
        var totalDbRows = existingBooks.size();
        var noOfLines = Math.min(maxLineNumber, totalDbRows);
        var destination = Paths.get(outputPath);
        Files.deleteIfExists(destination);
        // 2. Stream duplicate rows into CSV until target file size is reached
        try (var bw = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(destination, StandardOpenOption.CREATE, StandardOpenOption.WRITE),
                StandardCharsets.UTF_8))) {
            // CSV Header
            bw.write("id,title,author");
            bw.newLine();
            var index = 0;
            while (index <= noOfLines) {
                var book = existingBooks.get(index);
                // Format row with new sequential ID
                bw.write(String.format("%d,%s,%s",
                        book.getId(),
                        escapeCsvField(book.getTitle()),
                        escapeCsvField(book.getAuthor())
                ));
                bw.newLine();
                index++;
                if (index % 1000 == 0) {
                    bw.flush(); // Flush buffer periodically so Files.size() updates accurately
                }
            }
            bw.flush();
        }
        log.infof("The CSV is written in %d ms", System.currentTimeMillis() - inTime);
    }

    /**
     * Reads all existing records from DB and generates a CSV file of target MB size
     * by duplicating rows, assigning fresh IDs starting after max(id).
     *
     * @param outputPath Path where the generated CSV will be saved
     * @param targetMB   Target size in Megabytes (e.g., 10.0)
     */
    @SneakyThrows
    public void generateCsvFromDatabaseForSize(String outputPath, double targetMB) {
        // 1. Fetch existing records from DB
        var existingBooks = bookRepository.findAllBooks();
        if (existingBooks.isEmpty()) {
            throw new IllegalStateException("No records found in database.");
        }
        var inTime = System.currentTimeMillis();
        // 2. Determine max current ID to start fresh IDs safely
        var maxId = existingBooks.stream()
                .mapToLong(b -> Objects.nonNull(b.getId()) ? b.getId() : 0L)
                .max()
                .orElse(0L);
        var currentId = maxId + 1;
        var targetSizeBytes = (long) (targetMB * 1024 * 1024);
        var destination = Paths.get(outputPath);
        Files.deleteIfExists(destination);
        // 3. Stream duplicate rows into CSV until target file size is reached
        try (var bw = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(destination, StandardOpenOption.CREATE, StandardOpenOption.WRITE),
                StandardCharsets.UTF_8), 8 * 1024 * 1024)) {
            // CSV Header
            bw.write("id,title,author");
            bw.newLine();
            var index = 0;
            var totalDbRows = existingBooks.size();
            while (Files.size(destination) < targetSizeBytes) {
                var book = existingBooks.get(index);

                // Format row with new sequential ID
                bw.write(String.format("%d,%s,%s",
                        book.getId(),
                        escapeCsvField(book.getTitle()),
                        escapeCsvField(book.getAuthor())
                ));
                bw.newLine();
                currentId++;
                index = (index + 1) % totalDbRows; // Cycle through the DB records
                if (currentId % 1000 == 0) {
                    bw.flush(); // Flush buffer periodically so Files.size() updates accurately
                }
            }
            bw.flush();
        }
        log.infof("The CSV is written in %d ms", System.currentTimeMillis() - inTime);
    }

    private String escapeCsvField(String field) {
        if (Objects.isNull(field)) {
        	return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
