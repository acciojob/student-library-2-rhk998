package com.driver.services;

import com.driver.models.Book;
import com.driver.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;


import java.util.List;

@Service
public class BookService {


    @Autowired
    BookRepository bookRepository2;


    public List<Book> getBooks(String genre, boolean available, String author){
        List<Book> books = null; //find the elements of the list by yourself

        if (genre != null && author != null) {
            return bookRepository2.findBooksByGenreAuthor(genre, author, available);
        } else if (genre != null) {
            return bookRepository2.findBooksByGenre(genre, available);
        } else if (author != null) {
            return bookRepository2.findBooksByAuthor(author, available);
        } else {
            return bookRepository2.findByAvailability(available);
        }
    }

    public void createBook(Book book) {
        bookRepository2.save(book);
    }
}
