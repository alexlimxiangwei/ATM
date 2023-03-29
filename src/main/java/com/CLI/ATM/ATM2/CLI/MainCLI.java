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

    //region USER_MENU
    public void handleUserMenu(User theUser, Bank theBank){

        // print a summary of the user's accounts
        userCLI.printAccountsSummary(theUser);

        // print user menu
        Strings.print_UserMenu();

        // process the choice
        int choice = Util.readInt("Enter your choice: ",1, 5);

        switch (choice) {
            case 1 -> showTransHistory(theUser);
            case 2 -> updateFunds(theUser, WITHDRAW);
            case 3 -> updateFunds(theUser, DEPOSIT);
            case 4 -> transferFunds(theUser);
            case 5 -> showAccountSetting(theUser, theBank);
            case QUIT -> sc.nextLine(); // gobble up rest of previous input
        }

        // redisplay this menu unless the user wants to quit
        if (choice != QUIT) {
            handleUserMenu(theUser,theBank);
        }
        System.out.println("Logging out...\n");

    }
    //endregion

    /**
     * Show the transaction history for an account.
     * @param theUser	the logged-in User object
     */
    public void showTransHistory(User theUser) {

        int acctID;
        // get account whose transactions to print
        acctID = accountService.getInternalAccount(theUser, "view the transaction history of").getAccountID();

        // print the transaction history
        userService.printAcctTransHistory(theUser, acctID);
        System.out.println("Enter any key to continue");
        sc.nextLine();
        sc.nextLine();
    }

    //region ACCOUNT TRANSACTION FUNCTIONS
    /**
     * Process a fund withdraw from an account.
     * @param theUser	the logged-in User object
     */
    public void updateFunds(User theUser, int direction ) {

        Account fromAcct;
        double amount;
        double limit = Double.POSITIVE_INFINITY;
        String memo;
        String prompt;

        // get account to withdraw from
        if (direction == WITHDRAW) {
            prompt = "withdraw from";
        } else {
            prompt = "deposit to";
        }

        fromAcct = accountService.getInternalAccount(theUser, prompt);
        if (fromAcct == null){
            return;
        }
        if (direction == WITHDRAW) { // if making a withdrawal, set a limit
            // set transfer limit to whichever is lower :  current account balance or local transfer limit
            if (theUser.getLocal_transfer_limit() < fromAcct.getBalance()) {
                limit = userService.getLocalTransferLimit(theUser);
                if (limit == -1) {
                    System.out.println("Sorry, you have reached your daily withdrawal limit\nPress enter to continue.");
                    sc.nextLine();
                    sc.nextLine();
                    return;
                }
            }
        }
        // get amount to transfer
        amount = Util.readDouble(Strings.askAmountPrompt(limit), 0.1, limit);
        if (amount == QUIT) {
            return;
        }

        // make amount negative if withdrawing (WITHDRAW is = -1 while DEPOSIT is = 1)
        amount = direction * amount;
        // gobble up rest of previous input
        sc.nextLine();

        // get a memo
        memo = Util.readString("Enter a memo: ");

        // do the transfer
        // receiverID is -1 (TRANSACTION_TO_SELF) when it's an internal transaction (deposit/withdrawal)
        accountService.addTransactionToAcct(fromAcct, amount, TRANSACTION_TO_SELF, memo, LOCAL_TRANSACTION);

        accountService.addBalance(fromAcct, amount);
        // update balance on SQL
        SQLService.updateBalance(fromAcct.getBalance(), fromAcct.getAccountID());

    }
    public void transferFunds(User theUser) {
        Account fromAcct;
        Account toAcct;
        int toAcctID;
        int bankID;
        double amount;
        double transferLimit;
        boolean isLocalTransaction = true;

        Strings.print_transfer_fund_choices();
        int choice = Util.readInt("Enter your choice: ", 1, 2);
        if (choice == QUIT) {
            return;
        }
        // get account to transfer from
        fromAcct = accountService.getInternalAccount(theUser, "transfer from");
        if (fromAcct == null){
            return;
        }
        else if (fromAcct.getBalance() == 0){
            System.out.println("Sorry, you cannot transfer from an empty account.\nPlease press enter to continue.");
            sc.nextLine();
            return;
        }
        transferLimit = fromAcct.getBalance();

        if (choice == 1){
            // get internal account to transfer to
            toAcct = accountService.getInternalAccount(theUser, "transfer to");
            if (toAcct == null){
                return;
            }
            toAcctID = toAcct.getAccountID();

        }
        else{
            //get third party account to transfer to
            do {
                toAcctID = Util.readInt("Enter the account number of the account to transfer to: ");
                if (toAcctID == QUIT) {
                    return;
                }
                bankID = accountService.validateThirdPartyAccount(toAcctID);
                if (bankID == NOT_FOUND){
                    System.out.println("Sorry, the account number you entered is not found.\nPress enter to try again.");
                    sc.nextLine();
                }
            }while (bankID == NOT_FOUND);

            Bank toBank = bankService.getBankFromID(bankList, bankID);
            toAcct = bankService.getAccountFromID(toBank, toAcctID);

            if (toBank.isLocal() & transferLimit > theUser.getLocal_transfer_limit()){
                transferLimit = userService.getLocalTransferLimit(theUser); // if it's a local bank, apply limit for local transfer
                if (transferLimit == -1){
                    System.out.println("Sorry, you have reached your local transfer limit for today.\nPress enter to continue.");
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
                    return;
                }
            }

        }
        // if trying to transfer to same account
        if (toAcct == fromAcct){
            System.out.println("Sorry, you cannot transfer to the same account.\nPlease press enter to continue.");
            sc.nextLine();
            return;
        }
        // get amount to transfer
        amount = Util.readDouble(Strings.askAmountPrompt(transferLimit), 0.1, transferLimit);
        if (amount == QUIT) {
            return;
        }

        // get memo for transfer
        String memo = Util.readString("Enter memo for this transaction: ");

        // add transaction and update balance of fromAcct
        accountService.addTransactionToAcct(fromAcct, -amount, toAcctID, memo, isLocalTransaction);
        accountService.addBalance(fromAcct, -amount);
        SQLService.updateBalance(fromAcct.getBalance(), fromAcct.getAccountID());

        // add transaction and update balance of toAcct
        accountService.addTransactionToAcct(toAcct,amount, fromAcct.getAccountID(), memo, isLocalTransaction);
        accountService.addBalance(toAcct, amount);
        SQLService.updateBalance(toAcct.getBalance(), toAcctID);

        System.out.printf("$%.02f successfully transferred from %s to Account no: %d", amount, fromAcct.getName(), toAcctID);
    }


    //endregion

    //region ACCOUNT SETTINGS

    public void showAccountSetting(User theUser,Bank theBank) {
        int choice;

        // user menu
        print_AccountSettings();
        choice = Util.readInt("Enter your choice: ", 1, 4);

        // process the choice
        switch (choice) {
            case 1 -> changePassword(theUser, theBank);
            case 2 -> addAccount(theUser, theBank);
            case 3 -> changeAccountName(theUser);
            case 4 -> deleteAccount(theUser); // Not complete
            case QUIT -> sc.nextLine(); // gobble up rest of previous input
        }

        // redisplay this menu unless the user wants to quit
        if (choice != QUIT) {
            showAccountSetting(theUser,theBank);
        }
    }
    //change passwords
    public void changePassword(User theUser, Bank bank) {
        String pin;
        boolean is_validated;

        // get current password
        do {
            pin = Util.readString("Enter your current password: ");
            if (pin.equals(QUIT_STRING)) {
                return;
            }
            is_validated = userService.validatePin(theUser, pin);
            if (!is_validated) {
                System.out.println("Incorrect password, please try again.\n");
            }
        }while(!is_validated);

        // get new password
        do {
            pin = Util.readString("Enter new password: ");
            if (pin.equals(QUIT_STRING)) {
                return;
            }
            String confirm_Pin = Util.readString("Confirm new password:");
            if (confirm_Pin.equals(QUIT_STRING)) {
                return;
            }
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
     * Allows user to add a new account
     * @param theUser	the logged-in User object
     * @param currentBank the bank that user is from
     */
    public void addAccount(User theUser, Bank currentBank){
        String newAccName = Util.readString("Enter your new account name: ");
        if (newAccName.equals(QUIT_STRING)) {
            return;
        }

        Account newAccount = accountService.createAccount(newAccName, theUser, 0.00);
        theUser.getAccounts().add(newAccount);

        // Update add account on sql
        SQLService.addAccount(newAccount.getAccountID(), theUser.getCustomerID(), currentBank.getBankID(), newAccName, 0.00);
        System.out.printf("New account '%s' created successfully!\n", newAccName);
    }

    /**
     * Change user account name.
     * @param theUser	the logged-in User object
     */
    public void changeAccountName(User theUser) {
        Account acct = accountService.getInternalAccount(theUser,"change the name of");
        if (acct == null){
            return;
        }
        String newName = Util.readString("Enter new account name: ");
        // update locally
        acct.setName(newName);

        // Update on SQL
        SQLService.changeAccountName(acct.getAccountID(), newName);
        System.out.println("Account name successfully changed. ");
    }




    /**
     * Allow user to delete an account only when balance is 0.00
     * @param theUser	the logged-in User object
     */
    public void deleteAccount(User theUser){
        Account acc = accountService.getInternalAccount(theUser, "delete");
        if (acc == null){
            return;
        }
        if(acc.getBalance() > 0 ){
            System.out.println("Please make sure that your balance is 0 before deleting!\n");
            deleteAccount(theUser);
        }
        else {
            userService.deleteAccount(theUser, acc.getAccountID());
            // Update deleted account on sql
            SQLService.deleteAccount(acc.getAccountID());
            System.out.println("Account successfully deleted. ");
        }

    }//endregion
}
