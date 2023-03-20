package com.example.bank.service;

import com.example.bank.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.example.bank.util.Constants.conn;


@Component
public class TransactionService {



    /**
     * Create a new transaction
     * *@param *amount		the dollar amount transacted
     * *@param *accountID	    the account the transaction was made from
     * **@param *receiverID    the account the transaction was made to, -1 means deposit/withdraw
     * *@param *memo          deposit/withdraw
     */

    @Autowired
    TransactionService transactionService;

    public Transaction createTransaction(double amount, int accountID, int receiverID, String memo){

        var timestamp = new Date(new java.util.Date().getTime());
        var transactionID = transactionService.generateTransactionID();

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

    public void addTransactionToSQL(Transaction txn) {
        try {
            String strSelect = "insert into Transaction values(?, ?, ?, ? , ? , ?)";

            PreparedStatement stmt = conn.prepareStatement(strSelect);
            stmt.setInt(1, txn.getTransactionID());
            stmt.setInt(2, txn.getAccountID());
            stmt.setDouble(3,txn.getAmount());
            stmt.setDate(4, txn.getTimestamp());
            stmt.setInt(5, txn.getReceiverID());
            stmt.setString(6, txn.getMemo());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the biggest transaction number from SQL server, and returns +1 of it
     */
    public int generateTransactionID(){
        int max_id = 0;
        try {
            String strSelect = "select idTransaction from Transaction order by idTransaction desc limit 1;";
            PreparedStatement stmt = conn.prepareStatement(strSelect);
            ResultSet rset = stmt.executeQuery(strSelect);
            rset.next();
            max_id = rset.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return max_id + 1;
    }



}
