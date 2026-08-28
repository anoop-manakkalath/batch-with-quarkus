 package com.example.batch.entity;

import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_book")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookEntity extends PanacheEntityBase {
	
	@Id
    public Long id;

    @Column(name = "title", nullable = false)
    public String title;

    @Column(name = "author")
    public String author;
    
    public static List<BookEntity> findAllBooks() {
        return findAll().list();
    }
}
