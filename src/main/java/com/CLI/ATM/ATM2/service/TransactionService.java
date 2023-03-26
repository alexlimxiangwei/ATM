package com.CLI.ATM.ATM2.service;

import com.CLI.ATM.ATM2.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.CLI.ATM.ATM2.Constants.conn;

@Component
public class TransactionService {
    @Autowired
    SQLService SQLService;

    /**
     * Create a new transaction
     * *@param *amount		the dollar amount transacted
     * *@param *accountID	    the account the transaction was made from
     * **@param *receiverID    the account the transaction was made to, -1 means deposit/withdraw
     * *@param *memo          deposit/withdraw
     */
    public Transaction createTransaction(double amount, int accountID, int receiverID, String memo){

        var timestamp = new java.sql.Date(new java.util.Date().getTime());
        var transactionID = SQLService.generateTransactionID();

        return new Transaction(amount, timestamp, memo, accountID, receiverID, transactionID);
    }

    /**
     * Create a new transaction. This constructor is only used when importing transactions from SQL to local memory.
     * @param amount		the dollar amount transacted
     * @param receiverID	the account the transaction belongs to
     */

    public Transaction createTransactionFromSQL(int accountID, int transactionID, int receiverID, double amount, Date timestamp, String memo){
        return new Transaction(amount, timestamp, memo, accountID, receiverID, transactionID);
    }







}
