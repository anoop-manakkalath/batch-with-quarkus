package com.example.batch.processor;

import com.example.batch.entity.BookEntity;
import com.example.batch.mapper.BookMapper;
import com.example.batch.model.Book;

import jakarta.batch.api.chunk.ItemProcessor;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("bookItemProcessor")
@Dependent
public class BookItemProcessor implements ItemProcessor {
	
	private final BookMapper mapper;
	
	@Inject
	public BookItemProcessor(BookMapper mapper) {
		this.mapper = mapper;
	}
	
    @Override
    public BookEntity processItem(Object item) {
        return mapper.toEntity((Book) item);
    }
}
