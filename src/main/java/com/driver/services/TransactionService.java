package com.driver.services;

import com.driver.models.*;
import com.driver.repositories.BookRepository;
import com.driver.repositories.CardRepository;
import com.driver.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.driver.models.CardStatus.DEACTIVATED;

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
        //check whether bookId and cardId already exist
        //conditions required for successful transaction of issue book:
        //1. book is present and available

        if(cardRepository5.existsById(cardId) && bookRepository5.existsById(bookId)){
            Book book=bookRepository5.findById(bookId).get();
            Card card=cardRepository5.findById(cardId).get();

            if(book.isAvailable()==false){
                throw new Error("Book is either unavailable or not present");
            }
            if(card.getCardStatus()==DEACTIVATED){
                throw new Error("Card is invalid");
            }
            if(card.getBooks().size()>max_allowed_books){
                throw new Error("Book limit has reached for this card");
            }
            Transaction transaction=new Transaction();
            transaction.setTransactionId(UUID.randomUUID().toString());
            transaction.setBook(book);
            transaction.setCard(card);
            transaction.setIssueOperation(true);
            transaction.setTransactionDate(new Date());
            book.setAvailable(false);
            transactionRepository5.save(transaction);

            return transaction.getTransactionId();
        }

        return "Transition failed." ;//return transactionId instead
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
        long no_of_days= TimeUnit.DAYS.convert(issueTime,TimeUnit.MILLISECONDS);

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
