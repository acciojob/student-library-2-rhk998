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
    @PostMapping
    public ResponseEntity<String> addBook(@RequestBody Book book){
            bookService.createBook(book);
            return new ResponseEntity<>("SUCCESS",HttpStatus.ACCEPTED);
    }

    //Add required annotations

    @GetMapping("/get_book")
    public ResponseEntity<List<Book>> getBooks(
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "available", required = false, defaultValue = "false") boolean available,
            @RequestParam(value = "author", required = false) String author) {

        List<Book> bookList = bookService.getBooks(genre, available, author);

        if (bookList.isEmpty()) {
            return new ResponseEntity<>(bookList, HttpStatus.NO_CONTENT); // 204 if no books found
        }
        return new ResponseEntity<>(bookList, HttpStatus.OK);
    }

}
