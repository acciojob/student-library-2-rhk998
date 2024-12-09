package com.driver.repositories;

import com.driver.models.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Integer> {


    @Query("select b from Book b where b.available =:availability and b.author in (select a from Author a where a.name =:author_name)")
    List<Book> findBooksByAuthor(@Param("author_name") String authorName, @Param("availability") boolean availability);

    @Query("select b from Book b where b.genre =:genre and b.available =:available")
    List<Book> findBooksByGenre(@Param("genre") String genre, @Param("available") boolean available);

    @Query("select b from Book b where b.available =:available and b.genre =:genre and b.author in (select a from Author a where a.name =:author_name)")
    List<Book> findBooksByGenreAuthor(@Param("genre") String genre, @Param("author_name") String authorName, @Param("available") boolean available);

    @Query(value = "select * from book b where b.available =:availability", nativeQuery = true)
    List<Book> findByAvailability(@Param("availability") boolean availability);



    @Modifying
    @Transactional
    @Query("update Book b set b.available =:#{#book.available}, b.card =:#{#book.card} where b.id =:#{#book.id}")
    int updateBook(Book book);

    @Query("select b from Book b")
    List<Book> findAllBooks();


}
