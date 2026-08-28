package com.example.batch.writer;

import java.util.List;

import com.example.batch.entity.BookEntity;

import jakarta.batch.api.chunk.AbstractItemWriter;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

@Named("bookItemWriter")
@Dependent
public class BookItemWriter extends AbstractItemWriter {

    @Override
    @Transactional
    public void writeItems(List<Object> items) throws Exception {
    	items.forEach(item -> ((BookEntity) item).persist());
        BookEntity.getEntityManager().flush();
        BookEntity.getEntityManager().clear();
    }
}
