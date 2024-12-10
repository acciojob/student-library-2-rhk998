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

        List<Transaction> transactions = transactionRepository5.find(cardId, bookId,TransactionStatus.SUCCESSFUL, true);
        Transaction transaction = transactions.get(transactions.size()-1);

        Date d1=transaction.getTransactionDate();
        Date d2=new Date();
        long diff=d2.getTime()- d1.getTime();
        int dif=(int) diff;
        if(dif>15){
            fine_per_day=(fine_per_day)*(dif-15);
        }
        Book book=bookRepository5.findById(bookId).get();
        book.setAvailable(true);

        //for the given transaction calculate the fine amount considering the book has been returned exactly when this function is called
        //make the book available for other users
        //make a new transaction for return book which contains the fine amount as well



        Transaction returnBookTransaction  = new Transaction();

        returnBookTransaction.setId(transaction.getId());
        returnBookTransaction.setTransactionId(transaction.getTransactionId());
        returnBookTransaction.setCard(transaction.getCard());
        returnBookTransaction.setBook(transaction.getBook());
        returnBookTransaction.setIssueOperation(transaction.isIssueOperation());//check later
        returnBookTransaction.setTransactionStatus(transaction.getTransactionStatus());
        returnBookTransaction.setTransactionDate(transaction.getTransactionDate());

        return returnBookTransaction;
    }
}
