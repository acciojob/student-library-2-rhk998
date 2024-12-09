package com.driver.controller;

import com.driver.models.Book;
import com.driver.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//Add required annotations
@RestController
@RequestMapping("/book")
public class BookController {


    @Autowired
    BookService bookService;

    //Write createBook API with required annotations
    @PostMapping("/create")
    public ResponseEntity<String> createBook(@RequestBody Book book){
        bookService.createBook(book);
        return new ResponseEntity<>("Success",HttpStatus.ACCEPTED);
    }

    //Add required annotations
    @GetMapping
    public ResponseEntity<?> getBooks(
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "available", required = false, defaultValue = "false") boolean available,
            @RequestParam(value = "author", required = false) String author) {

        // Fetch books based on the provided parameters
        List<Book> bookList = bookService.getBooks(genre, available, author);

        // Check if the list is empty and return the appropriate response
        if (bookList.isEmpty()) {
            return new ResponseEntity<>("No books found matching the criteria.", HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(bookList, HttpStatus.OK);
    }
}
