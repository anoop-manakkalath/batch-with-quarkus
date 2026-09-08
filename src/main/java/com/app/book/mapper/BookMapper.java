package com.app.book.mapper;

import org.mapstruct.Mapper;

import com.app.book.entity.BookEntity;
import com.app.book.model.Book;

@Mapper(componentModel = "cdi")
public interface BookMapper {

    Book toDto(BookEntity entity);

    BookEntity toEntity(Book dto);
}
