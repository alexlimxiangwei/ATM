package com.CLI.ATM.ATM2.service;


import com.CLI.ATM.ATM2.CLI.AccountCLI;
import com.CLI.ATM.ATM2.Util;
import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.User;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;


@Component
public class UserService {

    @Autowired
    SQLService SQLService;

    @Autowired
    AccountCLI accountCLI;

    @Autowired
    TransactionService transactionService;

    public User createUserFromSQL(int idCustomer, String firstName, String lastName, String pin, double local_transfer_limit, double overseas_transfer_limit) {

        ArrayList<Account> accounts = new ArrayList<>();

        return new User(firstName, lastName, idCustomer, pin, accounts,local_transfer_limit, overseas_transfer_limit);
    }

    public User createNewUser(String firstName, String lastName, String pin, double local_transfer_limit, double overseas_transfer_limit) {
       var uuid = SQLService.generateNewCustomerID();
        ArrayList<Account> accounts = new ArrayList<>();
        var user = new User(firstName, lastName, uuid, pin, accounts, local_transfer_limit, overseas_transfer_limit);

        // print log message
        System.out.printf("New user %s, %s with ID %s created.\n",
                lastName, firstName, user.getCustomerID());

        return  user;

    }

    public void addAccountToUser(User user, Account acc) {
        user.getAccounts().add(acc);
    }

    public int numAccounts(User user) {
        return user.getAccounts().size();
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
        accountCLI.printTransHistory(user.getAccounts().get(acctIdx));
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
     * @param acctId get the acctId
     *
     */
    public void deleteAccount(User user, int acctId) {
        for (int acc_index = 0; acc_index < user.getAccounts().size(); acc_index++){
            if (user.getAccounts().get(acc_index).getAccountID() == acctId){
                user.getAccounts().remove(acc_index);
                break;
            }
        }
    }


    public double getLocalTransferLimit(User user){
        double recentLocalTransfers = transactionService.getRecentLocalTransfers(user);
        System.out.printf("Recent Local Transfers: $%.2f  your limit is: $%.2f.\n",recentLocalTransfers,user.getLocal_transfer_limit());
        if (recentLocalTransfers >= user.getLocal_transfer_limit()){
            return -1;
        }
        return user.getLocal_transfer_limit() - recentLocalTransfers;
    }
    public double getOverseasTransferLimit(User user){
        double recentOverseasTransfers = transactionService.getRecentOverseasTransfers(user);
        System.out.printf("Recent Overseas Transfers: $%.2f, your limit is: $%.2f.\n", recentOverseasTransfers, user.getOverseas_transfer_limit());
        if (recentOverseasTransfers >= user.getOverseas_transfer_limit()){
            return -1;
        }
        return user.getOverseas_transfer_limit() - recentOverseasTransfers;
    }
}


