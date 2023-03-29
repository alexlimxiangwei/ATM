package com.CLI.ATM.ATM2.service;

import com.CLI.ATM.ATM2.CLI.UserCLI;
import com.CLI.ATM.ATM2.Util;
import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.Bank;
import com.CLI.ATM.ATM2.model.Transaction;
import com.CLI.ATM.ATM2.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;

import static com.CLI.ATM.ATM2.Constants.*;


@Component
public class AccountService {

    @Autowired
    UserService userService;
    @Autowired
    TransactionService transactionService;
    @Autowired
    UserCLI userCli;
    @Autowired
    SQLService SQLService;
    @Autowired
    BankService bankService;
    @Autowired
    AccountService accountService;


    //region ACCOUNT_CREATION
    public Account createAccount(String name, User user, Double balance) {

        var accountID = SQLService.generateNewAccountID();

        var transactions= new ArrayList<Transaction>();

        return new Account(name, accountID, user, transactions, balance);
    }


    /**
     * Create new Account instance, with existing id (imported from SQL)
     * @param accountID existing accountID from sql
     * @param name		the name of the account
     * @param user	    the User object that holds this account
     * @param balance   starting balance
     */
    public Account importAccountFromSQL(int accountID, String name, User user, Double balance) {
        var transactions = new ArrayList<Transaction>();

        return new Account(name, accountID, user, transactions, balance);
    }


    /**
     * Add amount to the account that the user chooses
     * @param account account that user chooses
     * @param amount  amount that user inputs
     */
    public void addBalance(Account account, double amount){
        var balance = account.getBalance();
        account.setBalance(balance + amount);
    }
    //endregion


    //region THIRD_PARTY_TRANSFER
    /**
     * Get summary of the account
     * @param account account that user chooses
     */
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
     * @param account	    the account that user chooses
     * @param amount	    the amount that was transacted
     * @param receiverID    the recipient
     * @param memo	        adds a memo
     */
    public void addTransaction(Account account, double amount, int receiverID, String memo, boolean local) {

        // create new transaction and add it to our list
        Transaction newTrans = transactionService.createTransaction(amount, account.getAccountID(), receiverID, memo, local);
        // add transaction to SQL database too
        SQLService.addTransaction(newTrans);
        account.getTransactions().add(newTrans);

    }

    /**
     * Add a transaction in this account.
     * @param account	    the account that user chooses
     * @param transactionID	the transactionID
     * @param receiverID    the recipient
     * @param amount	    the amount
     * @param timestamp	    time when the transaction was made
     * @param memo	        adds a memo
     */
    public void addExistingTransaction(Account account, int transactionID, int receiverID, double amount, java.sql.Date timestamp, String memo, boolean local) {

        // create new transaction and add it to our list
        Transaction newTrans = transactionService.createTransactionFromSQL(account.getAccountID(), transactionID, receiverID, amount, timestamp, memo, local);
        account.getTransactions().add(newTrans);

    }



    /**
     * Gets an internal account of a user for transferring purposes
     * by asking for user input
     * @param theUser	the user to loop through his accounts
     * @param prompt prompt to ask, e.g. : transfer to / withdraw from
     * @return Account object for transferring of $
     */
    public Account getInternalAccount(User theUser, String prompt){
        int fromAcctIndex;
        int numOfAccounts = userService.numAccounts(theUser);
        userCli.printAccountsSummarySimp(theUser);
        String choice_prompt = String.format("\nEnter the number (1-%d) of the account to %s: ", numOfAccounts, prompt);
        fromAcctIndex = Util.readInt(choice_prompt, 1, numOfAccounts-1);
        if (fromAcctIndex == QUIT){
            return null;
        }
        fromAcctIndex--; // since index starts from 0
        Account fromAccount = userService.getAcct(theUser, fromAcctIndex);
        if (fromAccount == null){
            System.out.println("Invalid account. Please try again.");
            getInternalAccount(theUser, prompt);
        }
        return fromAccount;
    }

    /**
     * Validates whether an accountID exists or not.
     * Creates a new user if the account exists in SQL database
     * @param toAcctID accountID to validate
     * @return bankID that accountID belongs to or null if not found
     */
    public int validateThirdPartyAccount(int toAcctID){
        int bankID = AccountService.getBankIDFromAccountID(toAcctID);

        // if account doesn't exist in local memory, but exists in sql database,
        if (bankID == NOT_FOUND && SQLService.isSQLAccount(toAcctID)) {
            // add that user to local memory
            SQLService.addExistingUserByAcctId(toAcctID);
            bankID = getBankIDFromAccountID(toAcctID);
        }
        else if (bankID == NOT_FOUND) {
            // invalid account
            System.out.println("Invalid account. Please try again.");
        }
        // returns accountId and which bank it belongs to if it exists in local mem
        return bankID;
    }
    /**
     * Asks user for third party accountID to transfer to
     * @return User inputted accountID (may not be valid accountID)
     */
    public int getThirdPartyAccount() {
        System.out.println("Enter the account number of the account to transfer to: ");
        sc.nextLine();
        return sc.nextInt();
    }

    /**
     * Gets the bankID of the bank that an accountID belongs to.
     * returns -1 if not found
     */
    public static int getBankIDFromAccountID(int accountIDInput){
        int bankID = NOT_FOUND;
        for (Bank bank : bankList) {
            for (Account acc : bank.getAccounts()){
                if (acc.getAccountID() == accountIDInput){
                    bankID = bank.getBankID();
                    break;
                }
            }
        }
        return bankID;
    }
    //endregion


    //region LOGIN_&_SIGNUP




    /**
     * Sign up function
     * @param currentBank  bank that the user is in
     */
    public void handleSignUp(Bank currentBank){
        System.out.printf("Welcome to %s's sign up\n", currentBank.getName());

        // init class

        String fname = Util.readString("Enter First name:");
        if (fname.equals("-1")){
            return;
        }
        String lname = Util.readString("Enter last name:");
        if (lname.equals("-1")){
            return;
        }
        String newPin = Util.readString("Enter pin:");
        if (newPin.equals("-1")){
            return;
        }
        newPin = Util.hash(newPin);

//              Creates a new user account based on user input
        User newUser = bankService.addUserToBank(currentBank, fname, lname, newPin,
                DEFAULT_LOCAL_TRANSFER_LIMIT, DEFAULT_OVERSEAS_TRANSFER_LIMIT);
        Account newAccount2 = accountService.createAccount("CHECKING", newUser, 0.0);
        userService.addAccountToUser(newUser, newAccount2);
        bankService.addAccountToBank(currentBank, newAccount2);


        // Add new user to SQL
        SQLService.addNewUser(newUser.getCustomerID(),fname,lname,newPin,currentBank,
                DEFAULT_LOCAL_TRANSFER_LIMIT, DEFAULT_OVERSEAS_TRANSFER_LIMIT);
        SQLService.addAccount(newAccount2.getAccountID(),newUser.getCustomerID(),currentBank.getBankID(),"Savings",0.00);

        System.out.println("Account successfully created.");
    }
    //endregion

    public Account getAccountFromID(User user, int acctId){
        for (int acc_index = 0; acc_index < user.getAccounts().size(); acc_index++){
            if (user.getAccounts().get(acc_index).getAccountID() == acctId){
                return user.getAccounts().get(acc_index);
            }
        }
        return null;
    }
}
