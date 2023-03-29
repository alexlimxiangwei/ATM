package com.CLI.ATM.ATM2.service;

import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.Transaction;
import com.CLI.ATM.ATM2.model.User;
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

    //region TRANSACTION_CREATION
    /**
     * Create a new transaction
     * *@param *amount		the dollar amount transacted
     * *@param *accountID	    the account the transaction was made from
     * **@param *receiverID    the account the transaction was made to, -1 means deposit/withdraw
     * *@param *memo          deposit/withdraw
     */
    public Transaction createTransaction(double amount, int accountID, int receiverID, String memo, boolean local){

        var timestamp = new java.sql.Date(new java.util.Date().getTime());
        var transactionID = SQLService.generateTransactionID();

        return new Transaction(amount, timestamp, memo, accountID, receiverID, transactionID, local);
    }

    /**
     * Create a new transaction. This constructor is only used when importing transactions from SQL to local memory.
     * @param amount		the dollar amount transacted
     * @param receiverID	the account the transaction belongs to
     */

    public Transaction createTransactionFromSQL(int accountID, int transactionID, int receiverID, double amount, Date timestamp, String memo, boolean local){
        return new Transaction(amount, timestamp, memo, accountID, receiverID, transactionID, local);
    }

    //endregion

    //region GETTERS

    /**
     * Get the total amount of money that has been transferred locally in the last 24 hours
     * @param user the user to check
     * @return the total amount of money that has been transferred locally in the last 24 hours
     */
    public double getRecentLocalTransfers(User user){
        double recentLocalTransfers = 0;
        for (Account account : user.getAccounts()){
            for (Transaction txn : account.getTransactions()){
                // if transaction is local, within the last 24 hours, and is a negative amount
                if (txn.isLocal() && txn.getTimestamp().after(new Date(System.currentTimeMillis() - 86400000))
                        & txn.getAccountID() == account.getAccountID() & txn.getAmount() < 0){
                    recentLocalTransfers += txn.getAmount();
                }
            }
        }
        return -recentLocalTransfers;
    }

    /**
     * Get the total amount of money that has been transferred overseas in the last 24 hours
     * @param user the user to check
     * @return the total amount of money that has been transferred overseas in the last 24 hours
     */
    public double getRecentOverseasTransfers(User user){
        double recentOverseasTransfers = 0;
        for(Account account : user.getAccounts()){
            for (Transaction txn : account.getTransactions()){
                // if transaction is overseas, within the last 24 hours, and is a negative amount
                if (!txn.isLocal() && txn.getTimestamp().after(new Date(System.currentTimeMillis() - 86400000))
                        & txn.getAccountID() == account.getAccountID() & txn.getAmount() < 0){
                    recentOverseasTransfers += txn.getAmount();
                }
            }
        }
        return -recentOverseasTransfers;
    }
    //endregion

}
