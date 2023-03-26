package com.CLI.ATM.ATM2.service;

import com.CLI.ATM.ATM2.model.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Date;

import static org.mockito.Mockito.when;

class TransactionServiceTest {

    @Mock
    SQLService sqlService;

    @InjectMocks
    TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateTransaction() {
        double amount = 100.0;
        int accountID = 1;
        int receiverID = 2;
        String memo = "test";
        Date timestamp = new Date(new java.util.Date().getTime());
        int transactionID = 123;

        // Mock the SQLService's generateTransactionID() method
        when(sqlService.generateTransactionID()).thenReturn(transactionID);

        Transaction transaction = transactionService.createTransaction(amount, accountID, receiverID, memo);

        Assertions.assertEquals(amount, transaction.getAmount());
        Assertions.assertEquals(memo, transaction.getMemo());
        Assertions.assertEquals(accountID, transaction.getAccountID());
        Assertions.assertEquals(receiverID, transaction.getReceiverID());
        Assertions.assertEquals(transactionID, transaction.getTransactionID());
    }

    @Test
    void testCreateTransactionFromSQL() {
        int accountID = 1;
        int transactionID = 123;
        int receiverID = 2;
        double amount = 100.0;
        Date timestamp = new Date(new java.util.Date().getTime());
        String memo = "test";

        Transaction transaction = transactionService.createTransactionFromSQL(accountID, transactionID, receiverID, amount, timestamp, memo);

        Assertions.assertEquals(amount, transaction.getAmount());
        Assertions.assertEquals(timestamp, transaction.getTimestamp());
        Assertions.assertEquals(memo, transaction.getMemo());
        Assertions.assertEquals(accountID, transaction.getAccountID());
        Assertions.assertEquals(receiverID, transaction.getReceiverID());
        Assertions.assertEquals(transactionID, transaction.getTransactionID());
    }
}
