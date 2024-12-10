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
        Book book=bookRepository5.findById(bookId).get();
        Card card=cardRepository5.findById(cardId).get();

        Transaction transaction=new Transaction();

        transaction.setBook(book);
        transaction.setCard(card);
        transaction.setIssueOperation(true);


        if(book==null || !book.isAvailable())
        {
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository5.save(transaction);
            throw new Exception("Book is either unavailable or not present");
        }
        if(card==null || card.getCardStatus().equals(CardStatus.DEACTIVATED))
        {
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository5.save(transaction);
            throw new Exception("Card is invalid");
        }
        if(card.getBooks().size()>=max_allowed_books)
        {
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository5.save(transaction);
            throw new Exception("Book limit has reached for this card");
        }

        book.setCard(card);
        book.setAvailable(false);


        List<Book> currentBooks=card.getBooks();
        currentBooks.add(book);
        card.setBooks(currentBooks);

        cardRepository5.save(card);
        bookRepository5.updateBook(book);

        transaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);

        transactionRepository5.save(transaction);



        return transaction.getTransactionId();
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
