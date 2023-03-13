package com.CLI.ATM.ATM2.service;

import com.CLI.ATM.ATM2.DB_Util;
import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.Transaction;
import com.CLI.ATM.ATM2.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

@Component
public class AccountService {

    @Autowired
    DB_Util dbUtil;

    @Autowired
    TransactionService transactionService;


    public Account createAccount(String name, User user, Double balance) {

        var accountID = getNewAccountID(user);

        var transactions= new ArrayList<Transaction>();

        return new Account(name, accountID, user, transactions, balance);
    }


    /**
     * Create new Account instance, with existing id
     * *@param *accountID existing accountID from sql
     * *@param *name		the name of the account
     * *@param *holder	the User object that holds this account
     * *@param *balance starting balance
     */
    public Account createAccountExistingId(int accountID, String name, User user, Double balance) {
        var transactions = new ArrayList<Transaction>();

        return new Account(name, accountID, user, transactions, balance);
    }



    public void addBalance(Account account, double amount){
        var balance = account.getBalance();
        account.setBalance(balance + amount);
    }





    public int getNewAccountID(User user) {

        // init
        int id;
        Random rng = new Random();
        int len = 10;
        boolean nonUnique = false;

        // continue looping until we get a unique ID
        do {

            // generate the number
            id = 0;
            for (int c = 0; c < len; c++) {
                id += (rng.nextInt(10));
            }

            // check to make sure it's unique
            for (Account a : user.getAccounts()) {
                if (id == a.getAccountID()) {
                    nonUnique = true;
                    break;
                }
            }

        } while (nonUnique);

        return id;

//        Random random = new Random();
//        return (int) abs(((random.nextInt() % 900000000L) + 1000000000L));

    }

    public HashMap<String,String> getSummaryLine(Account account) {

        // get the account's balance
        double balance = account.getBalance();

        // summary value
        HashMap<String, String> val = new HashMap<String, String>();
        // format summary line depending on whether balance is negative

        val.put("balance", String.format("%.2f", balance));
        val.put("uuid", String.valueOf(account.getAccountID()));
        val.put("name", account.getName());

        return val;

    }

    /**
     * Add a new transaction in this account.
     * @param amount	the amount transacted
     * @param memo		the transaction memo
     */
    public void addTransaction(Account account, double amount, int receiverID, String memo) {

        // create new transaction and add it to our list
        Transaction newTrans = transactionService.createTransaction(amount, account.getAccountID(), receiverID, memo);
        // add transaction to SQL database too
        dbUtil.addTransactionToSQL(newTrans);
        account.getTransactions().add(newTrans);

    }


    public void addExistingTransaction(Account account, int transactionID, int receiverID, double amount, java.sql.Date timestamp, String memo) {

        // create new transaction and add it to our list
        Transaction newTrans = transactionService.createTransactionFromSQL(account.getAccountID(), transactionID, receiverID, amount, timestamp, memo);
        account.getTransactions().add(newTrans);

    }

    /**
     * Get summary line for account
     * @return	the summary line
     */


    /**
     * Print transaction history for account
     */
    //TODO : make this filo I/O to store all transactions
    public void printTransHistory(Account account) {

        System.out.printf("\nTransaction history for account %s\n", account.getAccountID());
        for (int t = account.getTransactions().size()-1; t >= 0; t--) {
            System.out.println(transactionService.getSummaryLine(account.getTransactions().get(t)));
        }
        System.out.println();

    }

}
