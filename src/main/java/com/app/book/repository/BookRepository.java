package com.app.book.repository;

import java.util.List;

import com.app.book.entity.BookEntity;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

@Repository
public interface BookRepository extends CrudRepository<BookEntity, Long> {

	@Query(value = "Book.findAll")
    List<BookEntity> findAll();
	
	@Query(value = "Book.findByTitleAndAuthor")
	List<BookEntity> findByTitleAndAuthor(String title, String author);
    
}
