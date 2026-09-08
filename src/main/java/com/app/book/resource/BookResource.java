package com.app.book.resource;

import java.util.List;

import com.app.book.model.Book;
import com.app.book.service.BookService;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.annotation.security.RolesAllowed;

@Controller("/api/books")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class BookResource {

    private final BookService bookService;

    public BookResource(BookService bookService) {
        this.bookService = bookService;
    }

    @Get("/")
    public List<Book> allBooks() {
        return bookService.findAllBooks();
    }

    @Get("/{id}")
    public Book getBook(Long id) {
        return bookService.findBookById(id);
    }

    @Post
    @RolesAllowed("admin")
    public Book addBook(@Body Book book) {
        return bookService.saveBook(book);
    }
    
    @Delete
    @RolesAllowed("admin")
    public void deleteBook(Long id) {
       bookService.deleBook(id);
    }
}
