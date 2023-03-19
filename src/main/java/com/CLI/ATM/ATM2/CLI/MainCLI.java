package com.CLI.ATM.ATM2.CLI;

import com.CLI.ATM.ATM2.Util;
import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.Bank;
import com.CLI.ATM.ATM2.model.Transaction;
import com.CLI.ATM.ATM2.model.User;
import com.CLI.ATM.ATM2.service.AccountService;
import com.CLI.ATM.ATM2.service.BankService;
import com.CLI.ATM.ATM2.service.TransactionService;
import com.CLI.ATM.ATM2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Scanner;

import static com.CLI.ATM.ATM2.Constants.DEPOSIT;
import static com.CLI.ATM.ATM2.Constants.WITHDRAW;

@Component
public class MainCLI {

    @Autowired
    UserService userService;

    @Autowired
    AccountService accountService;

    @Autowired
    Util util;

    @Autowired
    BankService bankService;

    @Autowired
    TransactionService transactionService;

    @Autowired
    UserCLI userCLI;

    public void displayBankSelectionPage(ArrayList<Bank> bankList) {
        System.out.println("Please select the bank you would like to use");
        for (int i = 0; i < bankList.size() ; i++){
            System.out.printf("  %d) %s\n", i + 1, bankList.get(i).getName());
        }
        System.out.print("Enter choice: ");

    }

    public void displaySignUpMenuPage(Bank currentBank) {
        System.out.printf("\nWelcome to %s !\n", currentBank.getName());
        System.out.println("What would you like to do?");
        System.out.println("  1) Log In");
        System.out.println("  2) Sign Up");
        System.out.print("Enter choice: ");
    }



    public User mainMenuPrompt(Bank theBank, Scanner sc) {

        // inits
        // test
        int userID;
        String pin;
        User authUser;

        // prompt user for user ID/pin combo until a correct one is reached
        do {
            System.out.print("Enter user ID: ");
            userID = sc.nextInt();
            sc.nextLine();

            System.out.print("Enter pin: ");
            pin = sc.nextLine();
            // try to get user object corresponding to ID and pin combo
            authUser = bankService.userLogin(theBank, userID, pin);
            if (authUser == null) {
                System.out.println("Incorrect user ID/pin combination. " +
                        "Please try again");
            }

        } while(authUser == null); 	// continue looping until we have a
        // successful login

        return authUser;
    }

    public void printUserMenu(ArrayList<Bank> bankList, User theUser, Bank theBank, Scanner sc) {

        // print a summary of the user's accounts
        userCLI.printAccountsSummary(theUser);

        // init
        int choice;

        // user menu
        do {
            System.out.println("What would you like to do?");
            System.out.println("  1) Show account transaction history");
            System.out.println("  2) Withdraw");
            System.out.println("  3) Deposit");
            System.out.println("  4) Transfer");
            System.out.println("  5) Account Setting");
            System.out.println("  6) Quit"); // TODO: password change/reset, settings
            System.out.println();
            System.out.print("Enter choice: ");
            choice = sc.nextInt();

            if (choice < 1 || choice > 6) {
                System.out.println("Invalid choice. Please choose 1-6.");
            }

        } while (choice < 1 || choice > 6);

        // process the choice
        switch (choice) {
            case 1 -> showTransHistory(theUser, sc);
            case 2 -> updateFunds(theUser, sc, WITHDRAW);
            case 3 -> updateFunds(theUser, sc, DEPOSIT);
            case 4 -> transferFunds(theUser,bankList, sc);
            case 5 -> showAccountSetting(theUser, sc, theBank);
            case 6 -> sc.nextLine(); // gobble up rest of previous input
        }

        // redisplay this menu unless the user wants to quit
        if (choice != 6) {
            printUserMenu(bankList, theUser,theBank, sc);
        }

    }

    public void transferFunds(User theUser, ArrayList<Bank> banks, Scanner sc) {
        Account fromAcct;
        Account toAcct = null;
        int toAcctID;
        int[] accountInfo;
        double amount;
        double transferLimit;

        int choice;
        do {
            System.out.println("Enter a choice below: ");
            System.out.println("1) Inter-account transfer");
            System.out.println("2) Third party transfer");
            choice = sc.nextInt();

        } while(choice < 0 || choice > 2);
        // get account to transfer from
        fromAcct = accountService.getInternalTransferAccount(theUser, "transfer from", sc);
        transferLimit = fromAcct.getBalance();

        if (choice == 1){
            // get internal account to transfer to
            toAcct = accountService.getInternalTransferAccount(theUser, "transfer to", sc);
            toAcctID = toAcct.getAccountID();
        }
        else{
            //get third party account to transfer to
            accountInfo = accountService.getThirdPartyTransferAccount(banks, sc);
            toAcctID = accountInfo[0];
            int bankID = accountInfo[1];
            if (bankID != -1){ // if account exists locally
                toAcct = bankService.getAccountById(banks.get(bankID), toAcctID);
            }
        }
        // get amount to transfer
        amount = accountService.getTransferAmount(transferLimit, sc);

        // get memo for transfer
        System.out.println("Enter memo for this transaction: ");
        String memo = sc.nextLine();

        // add transaction and update balance of fromAcct
        accountService.addTransaction(fromAcct, -amount, toAcctID, memo);
        accountService.addBalance(fromAcct, -amount);

        // if toAcct exists locally, add transaction locally + on sql, and update balance locally,
        if (toAcct != null) {
            accountService.addTransaction(toAcct,amount, fromAcct.getAccountID(), memo);
            accountService.addBalance(toAcct, amount);
        }
        else{ // if toAcct doesn't exist locally, only add transaction only on sql
            Transaction newTrans = transactionService.createTransaction(amount, toAcctID, fromAcct.getAccountID(), memo);
            transactionService.addTransactionToSQL(newTrans);
        }

        //update balance on SQL for both accounts
        accountService.updateSQLBalance(-amount, fromAcct.getAccountID());
        accountService.updateSQLBalance(amount, toAcctID);
    }

    public void showAccountSetting(User theUser, Scanner sc,Bank theBank) {
        int choice;

        // user menu
        do {
            System.out.println("Account Setting");
            System.out.println("  1) Change Password");
            System.out.println("  2) Create Account");
            System.out.println("  3) Change Account Type");
            System.out.println("  4) Delete Account");
            System.out.println("  5) Back");
            System.out.println();
            System.out.print("Enter choice: ");
            choice = sc.nextInt();

            if (choice < 1 || choice > 5) {
                System.out.println("Invalid choice. Please choose 1-6.");
            }

        } while (choice < 1 || choice > 5);

        // process the choice
        switch (choice) {
            case 1 -> changePassword(theUser, sc);
            case 2 -> addAccount(theUser, sc, theBank);
            case 3 -> changeAccountName(theUser,sc);
            case 4 -> deleteAccount(theUser,sc); // Not complete
            case 5 -> sc.nextLine(); // gobble up rest of previous input
        }

        // redisplay this menu unless the user wants to quit
        if (choice != 5) {
            showAccountSetting(theUser,sc,theBank);
        }
    }

    /**
     * Process a fund withdraw from an account.
     * @param theUser	the logged-in User object
     * @param sc		the Scanner object used for user input
     */
    public void updateFunds(User theUser, Scanner sc, int direction) {

        Account fromAcct;
        double amount;
        double withdrawLimit = -1;
        String memo;
        String directionString;

        // get account to withdraw from
        if (direction == WITHDRAW) {
            directionString = "withdraw from";
        }
        else{
            directionString = "transfer to";
        }

        fromAcct = accountService.getInternalTransferAccount(theUser, directionString, sc);
        if (direction == WITHDRAW){ // if making a withdraw, set a limit
            withdrawLimit = fromAcct.getBalance();
        }

        // get amount to transfer
        amount = accountService.getTransferAmount(withdrawLimit, sc);
        // make amount negative if withdrawing (WITHDRAW is = -1 while DEPOSIT is = 1)
        amount = direction * amount;
        // gobble up rest of previous input
        sc.nextLine();

        // get a memo
        System.out.print("Enter a memo: ");
        memo = sc.nextLine();

        // do the withdrawal
        // receiverID is -1 when it's an internal transaction (deposit/withdrawal)
        accountService.addTransaction(fromAcct, amount, -1, memo);

        accountService.addBalance(fromAcct, amount);
        // update balance on SQL
        accountService.updateSQLBalance(fromAcct.getBalance(),fromAcct.getAccountID());
    }

    /**
     * Show the transaction history for an account.
     * @param theUser	the logged-in User object
     * @param sc		the Scanner object used for user input
     */
    public void showTransHistory(User theUser, Scanner sc) {

        int theAcct;

        int numOfAcc = userService.numAccounts(theUser);

        // get account whose transactions to print
        do {
            System.out.println("\n");
            System.out.println("Account Transaction History");
            System.out.printf("Enter number (1-%d)\n", numOfAcc);

            int count = 1;
            for (Account acc: theUser.getAccounts()) {
                System.out.println(count + ") " + acc.getName());
                count++;
            }

            theAcct = sc.nextInt()-1;
            if (theAcct < 0 || theAcct >= numOfAcc) {
                System.out.println("Invalid account. Please try again.");
            }
        } while (theAcct < 0 || theAcct >= numOfAcc);

        // print the transaction history
        userService.printAcctTransHistory(theUser, theAcct);
    }

    //change passwords
    public void changePassword(User theUser, Scanner sc) {
        String pin;
        boolean is_validated;
        int numOfAcc = userService.numAccounts(theUser);

        sc.nextLine(); // remove empty line first
        do {
            System.out.println("Enter current password: ");
            pin = sc.nextLine();

            is_validated = userService.validatePin(theUser, pin);
            if (!is_validated) {
                System.out.println("Incorrect password, please try again. ");
            }
        }while(!is_validated);

        do {
            System.out.println("Enter new password: ");
            pin = sc.nextLine();
            System.out.println("Confirm new password: ");
            String confirm_Pin = sc.nextLine();
            is_validated = pin.equals(confirm_Pin);
            if (!is_validated) {
                System.out.println("The two passwords do not match, please try again.");

            }
        }while(!is_validated);
        theUser.setPinHash(pin);

        // Update password on SQL
        userService.changePassword(pin,numOfAcc);
        System.out.println("Password successfully changed.");



    }

    /**
     * Change user account name.
     * @param theUser	the logged-in User object
     * @param sc		the Scanner object used for user input
     */
    public void changeAccountName(User theUser, Scanner sc) {
        int numOfAcc = userService.numAccounts(theUser);
        System.out.print("Enter the accountNo which you would like to change the name: ");
        int usrChoice = sc.nextInt();
        System.out.print("Enter new account name: ");
        sc.nextLine();
        String newName = sc.nextLine();
        userService.changeAccountName(theUser, usrChoice, newName);

        // Update account name changes on sql
        accountService.changeAccountName(userService.getAcctUUID(theUser, usrChoice),numOfAcc, newName);
        System.out.println("Account name successfully changed. ");


    }

    /**
     * Allows user to add a new account
     * @param theUser	the logged-in User object
     * @param sc		the Scanner object used for user input
     * @param currentBank the bank that user is from
     */
    public void addAccount(User theUser, Scanner sc, Bank currentBank){
        int numOfAcc = userService.numAccounts(theUser);
        System.out.print("Enter your new account name: ");
        sc.nextLine();
        String newAcc = sc.nextLine();

        ArrayList<Account> existingAcc = theUser.getAccounts();
        int len = existingAcc.size();

        Account newAccount = accountService.createAccountExistingId(len, newAcc, theUser, 0.00);
        existingAcc.add(newAccount);

        // Update add account on sql
        accountService.addAccount(newAccount.getAccountID(),numOfAcc,currentBank.getBankID(), newAcc, 0.00);
        System.out.println("Account created successfully");

    }


    /**
     * Allow user to delete an account only when balance is 0.00
     * @param theUser	the logged-in User object
     * @param sc		the Scanner object used for user input
     */
    public void deleteAccount(User theUser, Scanner sc){
        Account acc = accountService.getInternalTransferAccount(theUser, "delete", sc);
        if(acc.getBalance() > 0 ){
            System.out.println("Please make sure that your balance is 0 before deleting! ");
        }
        else {
            userService.deleteAccount(theUser, acc.getAccountID());
            // Update deleted account on sql
            accountService.SQL_deleteAccount(acc.getAccountID()); //theUser.getUUID()
            System.out.println("Account successfully deleted. ");

        }



    }
}
