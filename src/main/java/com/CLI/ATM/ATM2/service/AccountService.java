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
    public void addTransaction(Account account, double amount, int receiverID, String memo) {

        // create new transaction and add it to our list
        Transaction newTrans = transactionService.createTransaction(amount, account.getAccountID(), receiverID, memo);
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
    public void addExistingTransaction(Account account, int transactionID, int receiverID, double amount, java.sql.Date timestamp, String memo) {

        // create new transaction and add it to our list
        Transaction newTrans = transactionService.createTransactionFromSQL(account.getAccountID(), transactionID, receiverID, amount, timestamp, memo);
        account.getTransactions().add(newTrans);

    }



    /**
     * Gets an internal account of a user for transferring purposes
     * by asking for user input
     * @param theUser	the user to loop through his accounts
     * @param directionString direction of transfer, e.g. : transfer to / withdraw from
     * @return Account object for transferring of $
     */
    public Account getInternalAccount(User theUser, String directionString){
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
     * Makes sure that the amount transferred is > than 0
     * @param amount	users inputted amount
     * @param limit     checks for the users daily limit
     */
    public String validateAmount(double amount, double limit) { // TODO: convert the 2 ifs below to throw custom exceptions
        if (amount < 0) {
            return "Amount must be greater than zero.";
        }
        if (limit != -1 && amount > limit) {
            return String.format("Amount must not be greater than balance of $%.02f.", limit);
        }
        return null;
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
     * Handles the login
     * @param currentBank currentBank user is in
     */
    public User handleLogIn(Bank currentBank){
        User curUser;
        do{
            curUser = accountService.loginPrompt(currentBank);

            if (curUser == null){
                System.out.println("Incorrect user ID/pin combination. " +
                        "Please try again");
            }
        }
        while(curUser == null);
        System.out.printf("Login Successful.\nWelcome, %s!", curUser.getFirstName());
        // stay in main menu until user quits
        return curUser;
    }

    /**
     * Prompts user to login
     * @param theBank  bank that the user is in
     */
    public User loginPrompt(Bank theBank) {
        // inits
        // test
        int userID;
        String pin;
        User authUser;

        // prompt user for user ID/pin combo until a correct one is reached

        System.out.print("Enter user ID: ");
        userID = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter pin: ");
        pin = sc.nextLine();
        // try to get user object corresponding to ID and pin combo
        authUser = bankService.userLogin(theBank, userID, pin);
        return authUser;
    }

    /**
     * Sign up function
     * @param currentBank  bank that the user is in
     */
    public void handleSignUp(Bank currentBank){
        System.out.printf("Welcome to %s's sign up\n", currentBank.getName());

        // init class
        User newUser = new User();

        System.out.print("Enter First name: ");
        String fname = sc.nextLine();
        newUser.setFirstName(fname);

        System.out.print("Enter last name: ");
        String lname = sc.nextLine();
        newUser.setLastName(lname);

        System.out.print("Enter pin: ");
        String newPin = Util.hash(sc.nextLine()); //hash it immediately, so we don't store the password at all

//              Creates a new user account based on user input
        User newUser2 = bankService.addUserToBank(currentBank, fname, lname, newPin,
                DEFAULT_LOCAL_TRANSFER_LIMIT, DEFAULT_OVERSEAS_TRANSFER_LIMIT);
        Account newAccount2 = accountService.createAccount("CHECKING", newUser2, 0.0);
        userService.addAccountToUser(newUser2, newAccount2);
        bankService.addAccountToBank(currentBank, newAccount2);

        System.out.println("Account successfully created.");

        // Add new user to SQL
        SQLService.addNewUser(newUser2.getCustomerID(),fname,lname,newPin,currentBank,
                DEFAULT_LOCAL_TRANSFER_LIMIT, DEFAULT_OVERSEAS_TRANSFER_LIMIT);
        SQLService.addAccount(newAccount2.getAccountID(),newUser2.getCustomerID(),currentBank.getBankID(),"Savings",0.00);
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
