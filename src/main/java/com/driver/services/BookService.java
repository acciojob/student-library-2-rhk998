package com.driver.services;

import com.driver.models.Author;
import com.driver.models.Book;
import com.driver.repositories.AuthorRepository;
import com.driver.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {


    @Autowired
    BookRepository bookRepository2;

    @Autowired
    AuthorRepository authorRepository;

    public void createBook(Book book){
//        int id =book.getAuthor().getId();
//        Author author=authorRepository.findById(id).get();
//        book.setAuthor(author);
//
//        List<Book> currentBooks=author.getBooksWritten();
//        currentBooks.add(book);
//        author.setBooksWritten(currentBooks);
//
//        authorRepository.save(author);

        bookRepository2.save(book);

        //bookRepository2.save(book);
    }



    public List<Book> getBooks(String genre, boolean available, String author){
        List<Book> books=new ArrayList<>();
        if(genre!=null && available==true){
            books=bookRepository2.findBooksByGenre(genre,true);
            books=bookRepository2.findBooksByAuthor(author,true);
            books=bookRepository2.findBooksByGenreAuthor(genre,null,true);
        }else if(genre!=null && author!=null && available==false){
            books=bookRepository2.findBooksByGenre(genre,false);
            books=bookRepository2.findBooksByAuthor(author,false);
            books=bookRepository2.findByAvailability(false);
        }
        //List<Book> books = null; //find the elements of the list by yourself
        return books;
    }
}
