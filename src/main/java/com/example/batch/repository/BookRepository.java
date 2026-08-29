package com.example.batch.repository;

import com.example.batch.entity.BookEntity;
import com.example.batch.mapper.BookMapper;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class BookRepository implements PanacheRepository<BookEntity> {

    @Inject
    BookMapper mapper;

    public List<com.example.batch.model.Book> findPagedBooks(int pageIndex, int pageSize) {
        return findAll()
                .page(Page.of(pageIndex, pageSize))
                .stream()
                .map(mapper::toDto)
                .toList();
    }
}
