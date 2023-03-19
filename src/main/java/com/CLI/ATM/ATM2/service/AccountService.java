package com.CLI.ATM.ATM2.service;

import com.CLI.ATM.ATM2.CLI.UserCLI;
import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.Bank;
import com.CLI.ATM.ATM2.model.Transaction;
import com.CLI.ATM.ATM2.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import static com.CLI.ATM.ATM2.Constants.conn;

@Component
public class AccountService {

    @Autowired
    UserService userService;

    @Autowired
    TransactionService transactionService;

    @Autowired
    UserCLI userCli;

    @Autowired
    BankService bankService;


    public Account createAccount(String name, User user, Double balance) {

        var accountID = generateNewAccountID();

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

    public void SQL_deleteAccount(int idAcc){ //int cust_has_id_customer
        try{
            String strUpdate = "delete from Transaction where Account_idAccount = ?;"; //Customer_idCustomer = ? AND
            PreparedStatement stmt = conn.prepareStatement(strUpdate);
            stmt.setInt(1, idAcc);
            stmt.executeUpdate();

            strUpdate = "delete from account where idAccount = ?;"; //Customer_idCustomer = ? AND
            stmt = conn.prepareStatement(strUpdate);
            stmt.setInt(1, idAcc);
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

    public void SQL_changeAccountName(int idAcc, int cust_has_id_customer, String name){
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
    public void SQL_addAccount(int idAcc, int cust_has_id_customer, int bank_id_bank, String name, double bal) {
        try {
            String strUpdate = "insert into account values(?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(strUpdate);
            stmt.setInt(1, idAcc);
            stmt.setInt(2, cust_has_id_customer);
            stmt.setInt(3, bank_id_bank);
            stmt.setString(4, name);
            stmt.setDouble(5,bal);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches all account balances and updates the balance of the accountId that user chooses
     * @param amount amount to set balance of account to
     * @param AcctID ID of account to update balance of
     */
    public void SQL_updateBalance(double amount, int AcctID) {
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
    /**
     * Gets an internal account of a user for transferring purposes
     * by asking for user input
     * @param theUser	the user to loop through his accounts
     * @param directionString direction of transfer, e.g. : transfer to / withdraw from
     * @return Account object for transferring of $
     */


    public Account getInternalTransferAccount(User theUser, String directionString, Scanner sc){
        int fromAcctIndex;
        int printSumFlag = 0;
        int numOfAccounts = userService.numAccounts(theUser);
        do {
            System.out.printf("Enter the number (1-%d) of the account to %s: ", numOfAccounts, directionString);
            while (printSumFlag != 1){
                userCli.printAccountsSummarySimp(theUser);
                printSumFlag +=1;
            }

            fromAcctIndex = sc.nextInt()-1;
            if (fromAcctIndex < 0 || fromAcctIndex >= numOfAccounts) {
                System.out.println("Invalid account. Please try again.");
            }
        } while (fromAcctIndex < 0 || fromAcctIndex >= numOfAccounts);

        return userService.getAcct(theUser, fromAcctIndex);

    }

    /**
     * Gets an account by asking user for an account ID
     * @param banks ArrayList of banks to loop through to look for an account
     * @return found accountId and found bankID, bankID is -1 if not in local memory
     */
    public int[] getThirdPartyTransferAccount(ArrayList<Bank> banks, Scanner sc){
        //get accountID to transfer to
        int toAcctIDInput;
        int toAcctID;
        boolean accountExists = false;
        int bankID = -1;
        do {
            System.out.println("Enter the account number of the account to " +
                    "transfer to: ");
            sc.nextLine();
            toAcctIDInput = sc.nextInt();
            //look in every bank to find the account
            for (int i = 0; i < banks.size() ; i++) {
                toAcctID = bankService.getAccountIndex(banks.get(i), toAcctIDInput);
                if (toAcctID != -1){ // if account is found:
                    accountExists = true;
                    bankID = i;
                    break;
                }
            }

            // if account doesn't exist in local memory, as well as in sql database,
            if (!accountExists && !userService.isAccount(toAcctIDInput)) {
                // invalid account
                System.out.println("Invalid account. Please try again.");
            }
        } while (!accountExists);
        // get accountId and which bank it belongs to if it exists in local mem
        return new int[] {toAcctIDInput, bankID};
    }

    /**
     * Gets amount to transfer from user, with an upper transfer limit
     * @param limit the upper $ amount limit of transfer, or -1 for no limit
     * @return the amount the user inputted
     */
    public double getTransferAmount(double limit, Scanner sc){
        double amount;
        do {
            if (limit == -1){
                System.out.print("Enter the amount to transfer: $");
            }
            else{
                System.out.printf("Enter the amount to transfer (max $%.02f): $",
                        limit);
            }
            amount = sc.nextDouble();
            if (amount < 0) {
                System.out.println("Amount must be greater than zero.");
            } else if (limit != -1 && amount > limit) { // check if transferring more than limit (-1 means no limit)
                System.out.printf("Amount must not be greater than balance " +
                        "of $%.02f.\n", limit);
            }
        } while (amount < 0 || (limit != -1 && amount > limit));
        return amount;
    }

    /**
     * Gets the biggest transaction number from SQL server, and returns +1 of it
     */
    public int generateNewAccountID(){
        int max_id = 0;
        try {
            String strSelect = "select idAccount from Account order by idAccount desc limit 1;";
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
