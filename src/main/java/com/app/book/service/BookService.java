package com.app.book.service;

import jakarta.inject.Singleton;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.app.book.entity.BookEntity;
import com.app.book.mapper.BookMapper;
import com.app.book.model.Book;
import com.app.book.repository.BookRepository;

@Singleton
public class BookService {

    private final BookRepository repository;
    private final BookMapper mapper;
    
    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    public BookService(BookRepository repository, BookMapper mapper) {
       this.repository = repository;
       this.mapper = mapper;
    }

    public List<Book> findAllBooks() {
        var bookEntities = repository.findAll();
    	var books = bookEntities.stream().map(mapper::toDto).toList();
    	log.atInfo().log("Fetched {} books for search criteria.", books.size());
    	return books;
    }
    
    public List<Book> searchBooks(String title, String author) {
    	var bookEntities = repository.findByTitleAndAuthor(title, author);
    	var books = bookEntities.stream().map(mapper::toDto).toList();
    	log.atInfo().log("Fetched {} books for search criteria.", books.size());
    	return books;
    }

    public Book findBookById(Long id) {
        var bookEntity = repository.findById(id);
        return bookEntity.map(mapper::toDto).get();
    }

    public Book saveBook(Book book) {
    	BookEntity bookEntity;
    	if (book.id() == 0L) {
    		bookEntity = repository.save(mapper.toEntity(book));
    	}
    	else {
    		bookEntity = repository.update(mapper.toEntity(book));
    	}
    	return mapper.toDto(bookEntity);
    }
    
    public void deleBook(long id) {
    	repository.deleteById(id);
    }
}
