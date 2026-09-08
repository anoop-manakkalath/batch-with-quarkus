 package com.app.book.entity;

import io.micronaut.data.annotation.MappedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@MappedEntity("tbl_book")
@NamedQuery(name = "Book.findAll", query = "SELECT b FROM BookEntity b")
@NamedQuery(name = "Book.findByTitleAndAuthor", query = "SELECT b FROM BookEntity b "
		+ "WHERE (:title IS NULL OR :title = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) "
		+ "AND (:author IS NULL OR :author = '' OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%')))")
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "author")
    private String author;
}
