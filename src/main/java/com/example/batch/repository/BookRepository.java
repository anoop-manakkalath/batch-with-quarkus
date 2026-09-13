package com.example.batch.repository;

import com.example.batch.entity.BookEntity;
import com.example.batch.mapper.BookMapper;
import com.example.batch.model.Book;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;

@ApplicationScoped
public class BookRepository implements PanacheRepository<BookEntity> {
	
	private final BookMapper mapper;
    
	@Inject
    public BookRepository(BookMapper mapper) {
    	this.mapper = mapper;
    }

    public List<Book> findAllBooks() {
        return findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<Book> findPagedBooks(int pageIndex, int pageSize) {
        return findAll()
                .page(Page.of(pageIndex, pageSize))
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<Book> searchBook(String title, String author) {
        var query = new StringBuilder("1=1");
        var params = new HashMap<String, Object>();
        if (StringUtils.isNotEmpty(title)) {
            query.append(" AND LOWER(title) LIKE :title");
            params.put("title", "%" + title.trim().toLowerCase() + "%");
        }
        if (StringUtils.isNotEmpty(author)) {
            query.append(" AND LOWER(author) LIKE :author");
            params.put("author", "%" + author.trim().toLowerCase() + "%");
        }
        return find(query.toString(), params).list()
                .stream()
                .map(mapper::toDto)
                .toList();
    }
        
    public long countBooks() {
        return count();
    }
}
