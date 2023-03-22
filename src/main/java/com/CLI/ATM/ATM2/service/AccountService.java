package com.CLI.ATM.ATM2.service;

import com.CLI.ATM.ATM2.CLI.UserCLI;
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
    static
    BankService bankService;


    public Account createAccount(String name, User user, Double balance) {

        var accountID = SQLService.generateNewAccountID();

        var transactions= new ArrayList<Transaction>();

        return new Account(name, accountID, user, transactions, balance);
    }


    /**
     * Create new Account instance, with existing id (imported from SQL)
     * *@param *accountID existing accountID from sql
     * *@param *name		the name of the account
     * *@param *holder	the User object that holds this account
     * *@param *balance starting balance
     */
    public Account importAccountFromSQL(int accountID, String name, User user, Double balance) {
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
        SQLService.addTransaction(newTrans);
        account.getTransactions().add(newTrans);

    }


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


    public Account getInternalTransferAccount(User theUser, String directionString){
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
     * @return found accountId and found bankID, bankID is -1 if not in local memory
     */
    public int[] getThirdPartyTransferAccount(){
        //get accountID to transfer to
        int toAcctID;
        int bankID;
        do {
            System.out.println("Enter the account number of the account to " +
                    "transfer to: ");
            sc.nextLine();
            toAcctID = sc.nextInt();
            bankID = getBankIDFromAccountID(toAcctID);

            // if account doesn't exist in local memory, but exists in sql database,
            if (bankID == NOT_FOUND && SQLService.isSQLAccount(toAcctID)) {
                // add that user to local memory
                SQLService.addExistingUserByAcctId(toAcctID);
                bankID = getBankIDFromAccountID(toAcctID);
            }
            else if (bankID == NOT_FOUND){
                // invalid account
                System.out.println("Invalid account. Please try again.");

            }
        } while (bankID == NOT_FOUND);
        // get accountId and which bank it belongs to if it exists in local mem
        return new int[] {toAcctID, bankID};
    }


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
}
