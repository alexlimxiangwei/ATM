package com.CLI.ATM.ATM2.CLI;

import com.CLI.ATM.ATM2.Strings;
import com.CLI.ATM.ATM2.Util;
import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.Bank;
import com.CLI.ATM.ATM2.model.User;
import com.CLI.ATM.ATM2.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static com.CLI.ATM.ATM2.Constants.*;
import static com.CLI.ATM.ATM2.Strings.*;

@Component
public class MainCLI {

    @Autowired
    UserService userService;
    @Autowired
    AccountService accountService;

    @Autowired
    BankService bankService;
    @Autowired
    SQLService SQLService;
    @Autowired
    UserCLI userCLI;

    //region BANK_MENU

    public void displayBankSelectionPage(ArrayList<Bank> bankList) {
        System.out.println("Please select the bank you would like to use");
        int bank_no = 1;
        for (Bank bank : bankList) {
            if (bank.isLocal()) {
                System.out.printf("  %d) %s\n", bank_no, bank.getName());
                bank_no++;
            }
        }
        System.out.print("Enter choice: ");
    }


    public void printUserMenu(ArrayList<Bank> bankList, User theUser, Bank theBank){

        // print a summary of the user's accounts
        userCLI.printAccountsSummary(theUser);

        // init
        int choice = Strings.displayUserMenu();
        // user menu


        // process the choice
        switch (choice) {
            case 1 -> showTransHistory(theUser);
            case 2 -> updateFunds(theUser, WITHDRAW);
            case 3 -> updateFunds(theUser, DEPOSIT);
            case 4 -> transferFunds(theUser,bankList);
            case 5 -> showAccountSetting(theUser, theBank);
            case 6 -> sc.nextLine(); // gobble up rest of previous input
        }

        // redisplay this menu unless the user wants to quit
        if (choice != 6) {
            printUserMenu(bankList, theUser,theBank);
        }

    }
    //endregion

    //region ACCOUNT_FUNCTIONS
    public void transferFunds(User theUser, ArrayList<Bank> banks) {
        Account fromAcct;
        Account toAcct;
        int toAcctID;
        int[] accountInfo;
        double amount;
        double transferLimit;
        int choice = transferFundsMenu();
        String amountValidationString;
        // get account to transfer from
        fromAcct = accountService.getInternalTransferAccount(theUser, "transfer from");
        transferLimit = fromAcct.getBalance();

        if (choice == 1){
            // get internal account to transfer to
            toAcct = accountService.getInternalTransferAccount(theUser, "transfer to");
            toAcctID = toAcct.getAccountID();
        }
        else{
            //get third party account to transfer to
            accountInfo = accountService.getThirdPartyTransferAccount();
            toAcctID = accountInfo[0];
            int bankID = accountInfo[1];

            Bank toBank = banks.get(bankID);
            toAcct = bankService.getAccountByID(toBank, toAcctID);
            // TODO: change the below to get remaining transfer limits instead of just the limit
            if (toBank.isLocal() & transferLimit > theUser.getLocal_transfer_limit()){
                transferLimit = theUser.getLocal_transfer_limit(); // if its a local bank, apply limit for local transfer
            }
            else if (transferLimit > theUser.getOverseas_transfer_limit()){
                transferLimit =theUser.getOverseas_transfer_limit(); // else, apply limit for overseas transfer
            }

        }
        do {
            // get amount to transfer
            Strings.print_AskAmount(transferLimit);
            amount = sc.nextDouble();
            // check if amount is > 0 and < limit
            amountValidationString = accountService.validateAmount(amount, transferLimit);
            // if invalid amount, print the error message
            if (amountValidationString != null){
                System.out.println(amountValidationString);
            }
        }while (amountValidationString != null);
        // get memo for transfer
        sc.nextLine();
        System.out.println("Enter memo for this transaction: ");
        String memo = sc.nextLine();

        // add transaction and update balance of fromAcct
        accountService.addTransaction(fromAcct, -amount, toAcctID, memo);
        accountService.addBalance(fromAcct, -amount);
        SQLService.updateBalance(fromAcct.getBalance(), fromAcct.getAccountID());

        // add transaction and update balance of toAcct
        accountService.addTransaction(toAcct,amount, fromAcct.getAccountID(), memo);
        accountService.addBalance(toAcct, amount);
        SQLService.updateBalance(toAcct.getBalance(), toAcctID);

        System.out.printf("$%.02f successfully transferred from %s to Account no: %d", amount, fromAcct.getName(), toAcctID);
    }



    /**
     * Process a fund withdraw from an account.
     * @param theUser	the logged-in User object
     */
    public void updateFunds(User theUser, int direction ) {

        Account fromAcct;
        double amount;
        double withdrawLimit = -1;
        String memo;
        String directionString;
        String amountValidationString;

        // get account to withdraw from
        if (direction == WITHDRAW) {
            directionString = "withdraw from";
        }
        else{
            directionString = "transfer to";
        }

        fromAcct = accountService.getInternalTransferAccount(theUser, directionString);
        if (direction == WITHDRAW){ // if making a withdraw, set a limit
            // set transfer limit to whichever is lower :  current account balance or local transfer limit
            // TODO: change this to check current transfer limit left (e.g. i alr withdrew $500 of my $1000 limit means my current limit for today is $500)
            if (theUser.getLocal_transfer_limit() < fromAcct.getBalance()){
                withdrawLimit = theUser.getLocal_transfer_limit();
            }
        }


        do {
            // get amount to transfer
            Strings.print_AskAmount(withdrawLimit);
            amount = sc.nextDouble();
            // check if amount is > 0 and < limit
            amountValidationString = accountService.validateAmount(amount, withdrawLimit);
            // if invalid amount, print the error message
            if (amountValidationString != null){
                System.out.println(amountValidationString);
            }
        }while (amountValidationString != null); // while invalid amount

        // make amount negative if withdrawing (WITHDRAW is = -1 while DEPOSIT is = 1)
        amount = direction * amount;
        // gobble up rest of previous input
        sc.nextLine();

        // get a memo
        System.out.print("Enter a memo: ");
        memo = sc.nextLine();

        // do the transfer
        // receiverID is -1 (TRANSACTION_TO_SELF) when it's an internal transaction (deposit/withdrawal)
        accountService.addTransaction(fromAcct, amount, TRANSACTION_TO_SELF, memo);

        accountService.addBalance(fromAcct, amount);
        // update balance on SQL
        SQLService.updateBalance(fromAcct.getBalance(),fromAcct.getAccountID());
    }

    /**
     * Show the transaction history for an account.
     * @param theUser	the logged-in User object
     */
    public void showTransHistory(User theUser) {

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
    //endregion

    //region ACCOUNT_SETTINGS

    public void showAccountSetting(User theUser,Bank theBank) {
        int choice;

        // user menu
        do {
            print_AccountSettings();
            choice = sc.nextInt();

            if (choice < 1 || choice > 5) {
                System.out.println("Invalid choice. Please choose 1-5.");
            }

        } while (choice < 1 || choice > 5);

        // process the choice
        switch (choice) {
            case 1 -> changePassword(theUser, theBank);
            case 2 -> addAccount(theUser, theBank);
            case 3 -> changeAccountName(theUser);
            case 4 -> deleteAccount(theUser); // Not complete
            case 5 -> sc.nextLine(); // gobble up rest of previous input
        }

        // redisplay this menu unless the user wants to quit
        if (choice != 5) {
            showAccountSetting(theUser,theBank);
        }
    }
    //change passwords
    public void changePassword(User theUser, Bank bank) {
        String pin;
        boolean is_validated;

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
        theUser.setPinHash(Util.hash(pin));

        // Update password on SQL
        SQLService.changePassword(pin,theUser.getCustomerID(), bank);
        System.out.println("Password successfully changed.");
    }

    /**
     * Change user account name.
     * @param theUser	the logged-in User object
     */
    public void changeAccountName(User theUser) {
        int numOfAcc = userService.numAccounts(theUser);
        System.out.print("Enter the accountNo which you would like to change the name: ");
        int usrChoice = sc.nextInt();
        System.out.print("Enter new account name: ");
        sc.nextLine();
        String newName = sc.nextLine();
        userService.changeAccountName(theUser, usrChoice, newName);

        // Update account name changes on sql
        SQLService.changeAccountName(userService.getAcctUUID(theUser, usrChoice),numOfAcc, newName);
        System.out.println("Account name successfully changed. ");
    }

    /**
     * Allows user to add a new account
     * @param theUser	the logged-in User object
     * @param currentBank the bank that user is from
     */
    public void addAccount(User theUser, Bank currentBank){
        int numOfAcc = userService.numAccounts(theUser);
        System.out.print("Enter your new account name: ");
        sc.nextLine();
        String newAccName = sc.nextLine();

        Account newAccount = accountService.createAccount(newAccName, theUser, 0.00);
        theUser.getAccounts().add(newAccount);

        // Update add account on sql
        SQLService.addAccount(newAccount.getAccountID(),numOfAcc,currentBank.getBankID(), newAccName, 0.00);
        System.out.printf("New account '%s' created successfully!\n", newAccName);

    }


    /**
     * Allow user to delete an account only when balance is 0.00
     * @param theUser	the logged-in User object
     */
    public void deleteAccount(User theUser){
        Account acc = accountService.getInternalTransferAccount(theUser, "delete");
        if(acc.getBalance() > 0 ){
            System.out.println("Please make sure that your balance is 0 before deleting! ");
        }
        else {
            userService.deleteAccount(theUser, acc.getAccountID());
            // Update deleted account on sql
            SQLService.deleteAccount(acc.getAccountID()); //theUser.getUUID()
            System.out.println("Account successfully deleted. ");

        }




    }//endregion
}
