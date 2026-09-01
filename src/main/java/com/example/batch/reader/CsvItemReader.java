package com.example.batch.reader;

import com.example.batch.model.Book;
import jakarta.batch.api.BatchProperty;
import jakarta.batch.api.chunk.AbstractItemReader;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Objects;

@Named("csvItemReader")
@JBossLog
@Dependent
public class CsvItemReader extends AbstractItemReader {

    private final String startOffsetProp;
    private final String endOffsetProp;
    private final String partitionIndexProp;
    private final String filePath;

    private RandomAccessFile file;
    private long endOffset;

    @Inject
    public CsvItemReader(
            @BatchProperty(name = "startOffset") String startOffsetProp,
            @BatchProperty(name = "endOffset") String endOffsetProp,
            @BatchProperty(name = "partitionIndex") String partitionIndexProp,
            @ConfigProperty(name = "batch.file-path", defaultValue = "data/large_books.csv") String filePath) {
        this.startOffsetProp = startOffsetProp;
        this.endOffsetProp = endOffsetProp;
        this.partitionIndexProp = partitionIndexProp;
        this.filePath = filePath;
    }

    @Override
    public void open(Serializable checkpoint) throws Exception {
        var startOffset = Long.parseLong(startOffsetProp);
        this.endOffset = Long.parseLong(endOffsetProp);
        var partitionIndex = Integer.parseInt(partitionIndexProp);
        this.file = new RandomAccessFile(Paths.get(filePath).toFile(), "r");
        if (partitionIndex == 0) {
            // Partition 0 starts at byte 0: skip CSV header line
            file.readLine();
        } else {
            // Other partitions: seek to assigned byte offset and discard partial line boundary
            file.seek(startOffset);
            file.readLine();
        }
    }

    @Override
    public Object readItem() throws Exception {
        return parseNextItem();
    }
    
    private Object parseNextItem() throws IOException {
    	 while (file.getFilePointer() < endOffset) {
             var rawLine = file.readLine();
             if (Objects.isNull(rawLine)) {
                 return null; // End of file
             }

             // Decode byte encoding fix (ISO-8859-1 -> UTF-8)
             var line = new String(rawLine.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8).trim();
             if (line.isBlank()) {
                 continue; // Skip blank lines without recursion
             }

             var firstComma = line.indexOf(',');
             var secondComma = line.indexOf(',', firstComma + 1);
             if (firstComma == -1 || secondComma == -1) {
                 continue; // Skip bad lines safely
             }

             try {
                 var id = Long.parseLong(line.substring(0, firstComma).trim());
                 var title = line.substring(firstComma + 1, secondComma).trim();
                 var author = line.substring(secondComma + 1).trim();
                 
                 return new Book(id, title, author);
             } catch (Exception e) {
                 continue;
             }
         }

         return null;
    }

    @Override
    public void close() throws Exception {
        if (Objects.nonNull(file)) {
            file.close();
            log.infof("Closed the CSV file: %s", filePath);
        }
    }
}
