package com.example.batch.mapper;

import org.mapstruct.Mapper;

import com.example.batch.entity.BookEntity;
import com.example.batch.model.Book;

@Mapper(componentModel = "jakarta")
public interface BookMapper {

    Book toDto(BookEntity entity);

    BookEntity toEntity(Book dto);
}
