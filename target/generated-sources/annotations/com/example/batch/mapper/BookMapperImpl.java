package com.example.batch.mapper;

import com.example.batch.entity.BookEntity;
import com.example.batch.model.Book;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-29T17:24:12+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260731-1305, environment: Java 21.0.12 (Eclipse Adoptium)"
)
@Singleton
@Named
public class BookMapperImpl implements BookMapper {

    @Override
    public Book toDto(BookEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Book.BookBuilder book = Book.builder();

        book.author( entity.author );
        book.id( entity.id );
        book.title( entity.title );

        return book.build();
    }

    @Override
    public BookEntity toEntity(Book dto) {
        if ( dto == null ) {
            return null;
        }

        BookEntity.BookEntityBuilder bookEntity = BookEntity.builder();

        bookEntity.author( dto.getAuthor() );
        bookEntity.id( dto.getId() );
        bookEntity.title( dto.getTitle() );

        return bookEntity.build();
    }
}
