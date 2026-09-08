package com.example.batch.service;

import com.example.batch.entity.BookEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class CsvFromDbGeneratorService {

    /**
     * Reads all existing records from DB and generates a CSV file of target MB size
     * by duplicating rows, assigning fresh IDs starting after max(id).
     *
     * @param outputPath Path where the generated CSV will be saved
     * @param targetMB   Target size in Megabytes (e.g., 10.0)
     */
    @Transactional
    @SneakyThrows
    public void generateCsvFromDatabase(String outputPath, double targetMB) {
        // 1. Fetch existing records from DB
        List<BookEntity> existingBooks = BookEntity.listAll();

        if (existingBooks.isEmpty()) {
            throw new IllegalStateException("No records found in database table 'tbl_book'.");
        }

        // 2. Determine max current ID to start fresh IDs safely (e.g., 152)
        var maxId = existingBooks.stream()
                .mapToLong(b -> Objects.nonNull(b.id) ? b.id : 0L)
                .max()
                .orElse(0L);

        var currentId = maxId + 1; // Starts at 152
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
                BookEntity book = existingBooks.get(index);

                // Format row with new sequential ID
                bw.write(String.format("%d,%s,%s",
                        currentId,
                        escapeCsvField(book.title),
                        escapeCsvField(book.author)
                ));
                bw.newLine();

                currentId++;
                index = (index + 1) % totalDbRows; // Cycle through the 151 DB records

                if (currentId % 5000 == 0) {
                    bw.flush(); // Flush buffer periodically so Files.size() updates accurately
                }
            }
            bw.flush();
        }
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
