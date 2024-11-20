package com.driver.services;

import com.driver.models.*;
import com.driver.repositories.BookRepository;
import com.driver.repositories.CardRepository;
import com.driver.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

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

        Book book = bookRepository5.findById(bookId).orElseThrow(() -> new Exception("Book is either unavailable or not present"));
        if(!book.isAvailable()){
            throw new Exception("Book is either unavailable or not present");
        }

        // If it fails: throw new Exception("Book is either unavailable or not present");
        //2. card is present and activated

        Card card = cardRepository5.findById(cardId).orElseThrow(()->new Exception("Card is invalid"));
        if(card.getCardStatus()!= CardStatus.ACTIVATED) throw new Exception("Card is invalid");

        // If it fails: throw new Exception("Card is invalid");

        //3. number of books issued against the card is strictly less than max_allowed_books

        if(card.getBooks().size() >= max_allowed_books){
            throw new Exception("Book limit has reached for this card");
        }

        // If it fails: throw new Exception("Book limit has reached for this card");

        //If the transaction is successful, save the transaction to the list of transactions and return the id
        book.setAvailable(false);
        book.setCard(card);
        bookRepository5.save(book);

        Transaction transaction = new Transaction();

        transaction.setBook(book);
        transaction.setCard(card);
        transaction.setIssueOperation(true);
        transaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);

        transactionRepository5.save(transaction);
        return transaction.getTransactionId();

        //Note that the error message should match exactly in all cases
    }

    public Transaction returnBook(int cardId, int bookId) throws Exception{

        List<Transaction> transactions = transactionRepository5.find(cardId, bookId, TransactionStatus.SUCCESSFUL, true);
        Transaction recenttransaction = transactions.get(transactions.size() - 1);

        //for the given transaction calculate the fine amount considering the book has been returned exactly when this function is called

        Book book = recenttransaction.getBook();
        book.setAvailable(true);
        book.setCard(null);
        bookRepository5.save(book);

        //make the book available for other users
        long borrowedDays = (System.currentTimeMillis() - recenttransaction.getTransactionDate().getTime()) / (1000 * 60 * 60 * 24);
        int fineAmount = (borrowedDays > getMax_allowed_days) ? (int) (borrowedDays - getMax_allowed_days) * fine_per_day : 0;

        //make a new transaction for return book which contains the fine amount as well
        Transaction returnTransaction = new Transaction();
        returnTransaction.setCard(recenttransaction.getCard());
        returnTransaction.setBook(book);
        returnTransaction.setIssueOperation(true);
        returnTransaction.setFineAmount(fineAmount);
        returnTransaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);

        transactionRepository5.save(returnTransaction);
        return returnTransaction; //return the transaction after updating all details
    }
}
