package com.CLI.ATM.ATM2.CLI;

import com.CLI.ATM.ATM2.Strings;
import com.CLI.ATM.ATM2.Util;
import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.Bank;
import com.CLI.ATM.ATM2.model.User;
import com.CLI.ATM.ATM2.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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




    public void handleUserMenu(User theUser, Bank theBank){

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
            case 4 -> transferFunds(theUser);
            case 5 -> showAccountSetting(theUser, theBank);
            case 6 -> sc.nextLine(); // gobble up rest of previous input
        }

        // redisplay this menu unless the user wants to quit
        if (choice != 6) {
            handleUserMenu(theUser,theBank);
        }

    }
    //endregion

    //region ACCOUNT_FUNCTIONS
    public void transferFunds(User theUser) {
        Account fromAcct;
        Account toAcct;
        int toAcctID;
        int bankID;
        double amount;
        double transferLimit;
        boolean isLocalTransaction = true;
        int choice = transferFundsMenu();
        String amountValidationString;
        // get account to transfer from
        fromAcct = accountService.getInternalAccount(theUser, "transfer from");
        transferLimit = fromAcct.getBalance();

        if (choice == 1){
            // get internal account to transfer to
            toAcct = accountService.getInternalAccount(theUser, "transfer to");
            toAcctID = toAcct.getAccountID();
        }
        else{
            //get third party account to transfer to
            do {
                toAcctID = accountService.getThirdPartyAccount();
                bankID = accountService.validateThirdPartyAccount(toAcctID);
            }while (bankID == NOT_FOUND);

            Bank toBank = bankService.getBankFromID(bankList, bankID);
            toAcct = bankService.getAccountFromID(toBank, toAcctID);

            // TODO: change the below to get remaining transfer limits instead of just the limit
            if (toBank.isLocal() & transferLimit > theUser.getLocal_transfer_limit()){
                transferLimit = userService.getLocalTransferLimit(theUser); // if it's a local bank, apply limit for local transfer
                if (transferLimit == -1){
                    System.out.println("Sorry, you have reached your local transfer limit for today.\nPress enter to continue.");
                    sc.nextLine();
                    sc.nextLine();
                    return;
                }
            }
            else if (!toBank.isLocal() & transferLimit > theUser.getOverseas_transfer_limit()){
                isLocalTransaction = false;
                transferLimit = userService.getOverseasTransferLimit(theUser); // else, apply limit for overseas transfer
                if (transferLimit == -1){
                    System.out.println("Sorry, you have reached your overseas transfer limit for today.\nPress enter to continue.");
                    sc.nextLine();
                    sc.nextLine();
                    return;
                }
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
        accountService.addTransaction(fromAcct, -amount, toAcctID, memo, isLocalTransaction);
        accountService.addBalance(fromAcct, -amount);
        SQLService.updateBalance(fromAcct.getBalance(), fromAcct.getAccountID());

        // add transaction and update balance of toAcct
        accountService.addTransaction(toAcct,amount, fromAcct.getAccountID(), memo, isLocalTransaction);
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

        fromAcct = accountService.getInternalAccount(theUser, directionString);
        if (direction == WITHDRAW){ // if making a withdraw, set a limit
            // set transfer limit to whichever is lower :  current account balance or local transfer limit
            if (theUser.getLocal_transfer_limit() < fromAcct.getBalance()){
                withdrawLimit = userService.getLocalTransferLimit(theUser);
                if (withdrawLimit == -1){
                    System.out.println("Sorry, you have reached your daily withdrawal limit\nPress enter to continue.");
                    sc.nextLine();
                    sc.nextLine();
                    return;
                }
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
        accountService.addTransaction(fromAcct, amount, TRANSACTION_TO_SELF, memo, LOCAL_TRANSACTION);

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
        sc.nextLine();
        System.out.println("Enter any key to continue");
        sc.nextLine();


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
        Account acct = accountService.getInternalAccount(theUser,"change the name of");
        System.out.print("Enter new account name: ");
        sc.nextLine();
        String newName = sc.nextLine();
        // update locally
        acct.setName(newName);

        // Update on SQL
        SQLService.changeAccountName(acct.getAccountID(), newName);
        System.out.println("Account name successfully changed. ");
    }

    /**
     * Allows user to add a new account
     * @param theUser	the logged-in User object
     * @param currentBank the bank that user is from
     */
    public void addAccount(User theUser, Bank currentBank){
        System.out.print("Enter your new account name: ");
        sc.nextLine();
        String newAccName = sc.nextLine();

        Account newAccount = accountService.createAccount(newAccName, theUser, 0.00);
        theUser.getAccounts().add(newAccount);

        // Update add account on sql
        SQLService.addAccount(newAccount.getAccountID(), theUser.getCustomerID(), currentBank.getBankID(), newAccName, 0.00);
        System.out.printf("New account '%s' created successfully!\n", newAccName);

    }


    /**
     * Allow user to delete an account only when balance is 0.00
     * @param theUser	the logged-in User object
     */
    public void deleteAccount(User theUser){
        Account acc = accountService.getInternalAccount(theUser, "delete");
        if(acc.getBalance() > 0 ){
            System.out.println("Please make sure that your balance is 0 before deleting! ");
        }
        else {
            userService.deleteAccount(theUser, acc.getAccountID());
            // Update deleted account on sql
            SQLService.deleteAccount(acc.getAccountID());
            System.out.println("Account successfully deleted. ");

        }




    }//endregion
}
