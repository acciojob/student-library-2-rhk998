package com.driver.services;

import com.driver.models.*;
import com.driver.repositories.BookRepository;
import com.driver.repositories.CardRepository;
import com.driver.repositories.TransactionRepository;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TransactionService {

    @Autowired
    BookRepository bookRepository5;

    @Autowired
    CardRepository cardRepository5;

    @Autowired
    TransactionRepository transactionRepository5;

    @Value("${books.max_allowed}")
    public int max_allowed_books;

    @Value("${books.max_allowed_days}")
    public int getMax_allowed_days;

    @Value("${books.fine.per_day}")
    public int fine_per_day;

    public String issueBook(int cardId, int bookId) throws Exception {
        Optional<Book> optionalBook = bookRepository5.findById(bookId);
        Optional<Card> optionalCard = cardRepository5.findById(cardId);

        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setIssueOperation(true);

        if (!optionalBook.isPresent() || !optionalBook.get().isAvailable()) {
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository5.save(transaction);
            throw new Exception("Book is either unavailable or not present");
        }

        if (!optionalCard.isPresent() || optionalCard.get().getCardStatus().equals(CardStatus.DEACTIVATED)) {
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository5.save(transaction);
            throw new Exception("Card is invalid");
        }

        Card card = optionalCard.get();
        Book book = optionalBook.get();

        if (card.getBooks() == null || card.getBooks().size() >= max_allowed_books) {
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository5.save(transaction);
            throw new Exception("Book limit has reached for this card");
        }

        book.setCard(card);
        book.setAvailable(false);

        card.getBooks().add(book);
        cardRepository5.save(card);
        bookRepository5.save(book);

        transaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        transactionRepository5.save(transaction);

        return transaction.getTransactionId();
    }


    public Transaction returnBook(int cardId, int bookId) throws Exception{

        //for the given transaction calculate the fine amount considering the book has been returned exactly when this function is called
        //make the book available for other users
        //make a new transaction for return book which contains the fine amount as well


        List<Transaction> transactions = transactionRepository5.find(cardId, bookId, TransactionStatus.SUCCESSFUL, true);
        Transaction transaction=transactions.get(transactions.size()-1);
        int fine=0;

        Date transactionDate=transaction.getTransactionDate();

        long issueTime=Math.abs(System.currentTimeMillis()-transactionDate.getTime());
        long no_of_days=TimeUnit.DAYS.convert(issueTime,TimeUnit.MILLISECONDS);

        if(no_of_days>getMax_allowed_days){
            fine=(int)(no_of_days-getMax_allowed_days)*fine_per_day;
        }

        Book book=transaction.getBook();
        book.setCard(null);
        book.setAvailable(true);

        Card card1=cardRepository5.findById(cardId).get();
        List<Book> bookList=card1.getBooks();

        bookList.remove(book);

        cardRepository5.save(card1);

        bookRepository5.updateBook(book);


        //   bookRepository5.updateBook(book);

        Transaction tr=new Transaction();
        tr.setBook(transaction.getBook());
        tr.setCard(transaction.getCard());
        tr.setIssueOperation(false);
        tr.setFineAmount(fine);
        tr.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        transactionRepository5.save(tr);


        return tr;

    }
}
