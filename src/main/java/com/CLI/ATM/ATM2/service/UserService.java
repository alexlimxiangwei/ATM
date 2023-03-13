package com.CLI.ATM.ATM2.service;


import com.CLI.ATM.ATM2.Util;
import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.Bank;
import com.CLI.ATM.ATM2.model.User;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;

import static com.CLI.ATM.ATM2.CLITools.adjustSpacing;

@Component
public class UserService {

    @Autowired
    BankService bankService;

    @Autowired
    AccountService accountService;

    public User createUserFromSQL(int idCustomer, String firstName, String lastName, String pin) {

        ArrayList<Account> accounts = new ArrayList<>();

        var user = new User(firstName, lastName, idCustomer, pin, accounts);

        // print log message
        System.out.printf("New user %s, %s with ID %s created.\n",
                lastName, firstName, idCustomer);

        return user;

    }

    public User createNewUser(String firstName, String lastName, String pin, Bank bank) {
       var uuid = bankService.getNewUserUUID(bank);
        ArrayList<Account> accounts = new ArrayList<>();
        var user = new User(firstName, lastName, uuid, pin, accounts);

        // print log message
        System.out.printf("New user %s, %s with ID %s created.\n",
                lastName, firstName, user.getUuid());

        return  user;

    }

    public void addAccountToUser(User user, Account acc) {
        user.getAccounts().add(acc);
    }

    public int numAccounts(User user) {
        return user.getAccounts().size();
    }

    /**
     * Get the balance of a particular account.
     * @param acctIdx	the index of the account to use
     * @return			the balance of the account
     */
    public double getAcctBalance(User user, int acctIdx) {
        return user.getAccounts().get(acctIdx).getBalance();
    }

    /**
     * Get the UUID of a particular account.
     *
     * @param acctIdx the index of the account to use
     * @return the UUID of the account
     */
    public int getAcctUUID(User user, int acctIdx) {
        return user.getAccounts().get(acctIdx).getAccountID();
    }
    /**
     * Get a particular account.
     * @param acctIndex	the index of the account to use
     * @return			The account object
     */
    public Account getAcct(User user, int acctIndex) {
        return user.getAccounts().get(acctIndex);
    }
    /**
     * Print transaction history for a particular account.
     * @param acctIdx	the index of the account to use
     */
    public void printAcctTransHistory(User user, int acctIdx) {
        accountService.printTransHistory(user.getAccounts().get(acctIdx));
    }



    /**
     * Check whether a given pin matches the true User pin
     * @param aPin	the pin to check
     * @return		whether the pin is valid or not
     */
    public boolean validatePin(User user, String aPin) {
        return Util.hash(aPin).equals(user.getPinHash());
    }


    /**
     * prints a simplified accounts summary in the event user forgets what is their source and destination account
     */
    public void printAccountsSummarySimp(User user){
        System.out.printf("\n\n%s's accounts summary\n", user.getFirstName());
        for (int a = 0; a < user.getAccounts().size(); a++) {
            HashMap<String, String> summary = accountService.getSummaryLine(user.getAccounts().get(a));
            System.out.printf("%d) Name: %-18s |Balance: %-18s\n", a+1,
                    summary.get("name"), summary.get("balance"));
        }
    }


    /**
     * Print summaries for the accounts of this user.
     */

    public void printAccountsSummary(User user) {

        System.out.printf("\n\n%s %s's accounts summary\n", user.getFirstName(), user.getLastName());
        System.out.print(
                """
                        ╔════════════════════╦════════════════════╦════════════════════╗
                        ║ Name               ║ Account ID         ║ Balance            ║
                        """
        );

        for (Account account : user.getAccounts()) {
            System.out.println("╠════════════════════╬════════════════════╬════════════════════╣");
            HashMap<String, String> val = accountService.getSummaryLine(account);

            System.out.printf(
                    "║ %-18s║ %-18s║ %18s║\n",
                    adjustSpacing(val.get("name")),
                    adjustSpacing(val.get("uuid")),
                    adjustSpacing("$" + val.get("balance")));

        }
        System.out.println("╚════════════════════╩════════════════════╩════════════════════╝\n");
    }



    /**
     * Allow user to change account name
     * @param acctIdx get the acctId
     * @param name name that user want to change the account to
     */
    public void changeAccountName(User user, int acctIdx, String name) {
        Account account = user.getAccounts().get(acctIdx);
        account.setName(name);
    }

    /**
     * Allow user to change account name
     * @param acctIdx get the acctId
     *
     */
    public void deleteAccount(User user, int acctIdx) {
        user.getAccounts().remove(acctIdx);
    }



}


