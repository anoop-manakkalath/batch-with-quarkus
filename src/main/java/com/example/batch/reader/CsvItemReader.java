package com.example.batch.reader;

import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import com.example.batch.model.Book;

import jakarta.batch.api.BatchProperty;
import jakarta.batch.api.chunk.AbstractItemReader;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("csvItemReader")
@Dependent
public class CsvItemReader extends AbstractItemReader {

    String startOffsetProp;
    String endOffsetProp;
    String partitionIndexProp;

    private RandomAccessFile file;
    private long endOffset;
    private int partitionIndex;
    
    @Inject
    public CsvItemReader(
    		@BatchProperty(name = "startOffset") String startOffsetProp,
            @BatchProperty(name = "endOffset") String endOffsetProp,
            @BatchProperty(name = "partitionIndex") String partitionIndexProp) {
    	this.startOffsetProp = startOffsetProp;
    	this.endOffsetProp = endOffsetProp;
    	this.partitionIndexProp = partitionIndexProp;
    }

    @Override
    public void open(Serializable checkpoint) throws Exception {
        var startOffset = Long.parseLong(startOffsetProp);
        this.endOffset = Long.parseLong(endOffsetProp);
        this.partitionIndex = Integer.parseInt(partitionIndexProp);

        this.file = new RandomAccessFile(Paths.get("data/large_books.csv").toFile(), "r");

        if (partitionIndex == 0) {
            // First thread: starts at byte 0 and skips CSV header line
            file.readLine();
        } else {
            // Worker threads: jump to assigned start byte and skip partial line fragment
            file.seek(startOffset);
            file.readLine(); 
        }
    }

    @Override
    public Object readItem() throws Exception {
        // Stop reading once current pointer position passes endOffset
        if (file.getFilePointer() >= endOffset) {
            return null;
        }
        var rawLine = file.readLine();
        if (rawLine == null) {
            return null; // EOF reached
        }

        // Decode string from ISO-8859-1 byte read to UTF-8
        var line = new String(rawLine.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8).trim();
        if (line.isBlank()) {
            return readItem();
        }

        var firstComma = line.indexOf(',');
        var secondComma = line.indexOf(',', firstComma + 1);
        if (firstComma == -1 || secondComma == -1) {
            return readItem();
        }

        try {
            var id = Long.parseLong(line.substring(0, firstComma).trim());
            var title = line.substring(firstComma + 1, secondComma).trim();var author = line.substring(secondComma + 1).trim();

            return new Book(id, title, author);
        } catch (Exception e) {
            return readItem();
        }
    }

    @Override
    public void close() throws Exception {
        if (file != null) {
        	file.close();
        }
    }
}
