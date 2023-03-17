package com.CLI.ATM.ATM2.service;

import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.Bank;
import com.CLI.ATM.ATM2.model.Transaction;
import com.CLI.ATM.ATM2.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static com.CLI.ATM.ATM2.Constants.conn;

@Component
public class AccountService {

    @Autowired
    UserService userService;

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
        HashMap<String, String> val = new HashMap<>();
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
        transactionService.addTransactionToSQL(newTrans);
        account.getTransactions().add(newTrans);

    }


    public void addExistingTransaction(Account account, int transactionID, int receiverID, double amount, java.sql.Date timestamp, String memo) {

        // create new transaction and add it to our list
        Transaction newTrans = transactionService.createTransactionFromSQL(account.getAccountID(), transactionID, receiverID, amount, timestamp, memo);
        account.getTransactions().add(newTrans);

    }


    /*
     * delete user's specified account from sql database
     * idAcc creates new account with uuid
     * cust_has_id_customer creates new account with fname
     * bank_id_bank creates new account with lname
     * name creates new account with pin
     */

    public void deleteAccount(int idAcc){ //int cust_has_id_customer
        try{
            String strSelect = "delete from account where idAccount = ?"; //Customer_idCustomer = ? AND
            PreparedStatement stmt = conn.prepareStatement(strSelect);
            stmt.setInt(1, idAcc);
            //stmt.setInt(2, cust_has_id_customer);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Update user's new account name to sql database
     * @param idAcc get the uuid
     * @param cust_has_id_customer gets the customerID
     * @param name the name the user wants to change to
     */

    public void changeAccountName(int idAcc, int cust_has_id_customer, String name){
        try{
            String strSelect = "update account set name = ? where Customer_idCustomer = ? AND idAccount = ?";
            PreparedStatement stmt = conn.prepareStatement(strSelect);
            stmt.setString(1, name);
            stmt.setInt(2, idAcc);
            stmt.setInt(3, cust_has_id_customer);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add new account to sql database
     * @param idAcc creates new account with uuid
     * @param cust_has_id_customer gets the customerID
     * @param bank_id_bank gets the bankID
     * @param name creates new account with pin
     */
    public void addAccount(int idAcc, int cust_has_id_customer, int bank_id_bank, String name, double bal) {
        try {
            String strUpdate = "insert into account values(?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(strUpdate);
            stmt.setInt(1, idAcc);
            stmt.setInt(2, cust_has_id_customer);
            stmt.setInt(3, bank_id_bank);
            stmt.setString(4, name);
            stmt.setDouble(5,bal);
            stmt.executeUpdate();

            System.out.println(stmt);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches all account balances and updates the balance of the accountId that user chooses
     * @param amount amount to set balance of account to
     * @param AcctID ID of account to update balance of
     */
    public void updateSQLBalance(double amount, int AcctID) {
        try {
            String strSelect = "update account set balance = ? where idAccount = ?";
            PreparedStatement stmt = conn.prepareStatement(strSelect);
            stmt.setDouble(1, amount);
            stmt.setInt(2, AcctID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches all existing transactions from sql for a particular account, and adds it to the account
     * @param acc account to search for transactions from.
     */
    public void addAllTransactionsToAccount(Account acc){
        try {
            Statement stmt = conn.createStatement();
            int id = acc.getAccountID();
            String strSelect = String.format("select * from Transaction where Account_idAccount =" +
                    " %d or receiverID = %d order by idTransaction;", id, id);
            ResultSet rset = stmt.executeQuery(strSelect);
            while (rset.next()){
                int idTransaction = rset.getInt("idTransaction");
                int idAccount = rset.getInt("Account_idAccount");
                int receiverID = rset.getInt("receiverID");
                Date date = rset.getDate("timeStamp");
                double amount = rset.getDouble("amount");
                String memo = rset.getString("memo");

                if (idAccount == id) {
                    addExistingTransaction(acc, idTransaction, receiverID, amount, date, memo);
                }
                else{
                    addExistingTransaction(acc, idTransaction, receiverID, amount * -1, date, memo);
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches all existing accounts from sql for a particular user/customer and bank, and adds it to the bank
     * Called when user is found in DB_Util.findUser()
     * @param user user to search for accounts from.
     * @param bank bank to search for accounts from.
     */
    public void addAccountsToUser(User user, Bank bank) {

        try {
            Statement stmt = conn.createStatement();
            String strSelect = String.format("select idAccount,name,balance from Account where Customer_idCustomer = %d and Bank_idBank = %d;",user.getUuid(), bank.getBankID());

            ResultSet rset = stmt.executeQuery(strSelect);
            while (rset.next()){
                int idAccount = rset.getInt("idAccount");
                System.out.println(idAccount);
                String name = rset.getString("name");
                double balance = rset.getDouble("balance");
                Account newAccount = createAccountExistingId(idAccount, name, user, balance);
                // fetch and add all transactions for the account from sql
                //here
                addAllTransactionsToAccount(newAccount);
                userService.addAccountToUser(user, newAccount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


}
