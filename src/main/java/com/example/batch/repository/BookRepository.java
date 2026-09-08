package com.example.batch.repository;

import com.example.batch.entity.BookEntity;
import com.example.batch.mapper.BookMapper;
import com.example.batch.model.Book;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class BookRepository implements PanacheRepository<BookEntity> {
	
	private final BookMapper mapper;
    
	@Inject
    public BookRepository(BookMapper mapper) {
    	this.mapper = mapper;
    }

    public List<Book> findPagedBooks(int pageIndex, int pageSize) {
        return findAll()
                .page(Page.of(pageIndex, pageSize))
                .stream()
                .map(mapper::toDto)
                .toList();
    }
        
    public long countBooks() {
        return count();
    }
}
