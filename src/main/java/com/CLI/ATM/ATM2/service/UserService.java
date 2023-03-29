package com.CLI.ATM.ATM2.service;


import com.CLI.ATM.ATM2.CLI.AccountCLI;
import com.CLI.ATM.ATM2.Util;
import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.Bank;
import com.CLI.ATM.ATM2.model.User;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static com.CLI.ATM.ATM2.Constants.*;
import static com.CLI.ATM.ATM2.Constants.DEFAULT_OVERSEAS_TRANSFER_LIMIT;


@Component
public class UserService {
    //region INIT
    @Autowired
    SQLService SQLService;

    @Autowired
    AccountCLI accountCLI;

    @Autowired
    BankService bankService;
    @Autowired
    AccountService accountService;

    @Autowired
    UserService userService;

    @Autowired
    TransactionService transactionService;
    //endregion

    //region USER_CREATION
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

    /**
     * Sign up function
     * @param currentBank  bank that the user is in
     */
    public void handleSignUp(Bank currentBank){
        System.out.printf("Welcome to %s's sign up\n", currentBank.getName());

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

    //region GETTTERS
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
     * Get the remaining local transfer limit for the user
     * @param user the user to check
     * @return the remaining local transfer limit
     */
    public double getLocalTransferLimit(User user){
        double recentLocalTransfers = transactionService.getRecentLocalTransfers(user);
        System.out.printf("Recent Local Transfers: $%.2f  your limit is: $%.2f.\n",recentLocalTransfers,user.getLocal_transfer_limit());
        if (recentLocalTransfers >= user.getLocal_transfer_limit()){
            return -1;
        }
        return user.getLocal_transfer_limit() - recentLocalTransfers;
    }

    /**
     * Get the remaining overseas transfer limit for the user
     * @param user the user to check
     * @return the remaining overseas transfer limit
     */
    public double getOverseasTransferLimit(User user){
        double recentOverseasTransfers = transactionService.getRecentOverseasTransfers(user);
        System.out.printf("Recent Overseas Transfers: $%.2f, your limit is: $%.2f.\n", recentOverseasTransfers, user.getOverseas_transfer_limit());
        if (recentOverseasTransfers >= user.getOverseas_transfer_limit()){
            return -1;
        }
        return user.getOverseas_transfer_limit() - recentOverseasTransfers;
    }

    //endregion

    //region SETTER
    /**
     * Allow user to change account name
     * @param acctIdx get the acctId
     * @param name name that user want to change the account to
     */
    public void changeAccountName(User user, int acctIdx, String name) {
        Account account = user.getAccounts().get(acctIdx);
        account.setName(name);
    }
    //endregion

    //region ACCOUNT DELETE

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

    //endregion

    //region LOGIN
    /**
     * Get the User object associated with a particular userID and pin, if they
     * are valid.
     * @param userID	the user UUID to log in
     * @param pin		the associate pin of the user
     * @return			the User object, if login is successfully, or null, if
     * 					it is not
     */

    public User userLogin(Bank bank, int userID, String pin) {

        // search through list of users
        for (User u : bank.getUsers()) {

            // if we find the user, and the pin is correct, return User object
            if (u.getCustomerID() == userID)
            {
                if (validatePin(u, pin)){
                    return u;
                }
                else {
                    return null;
                }
            }
        }
        //If userId isn't found locally, search sql database
//        System.out.println("User not found locally, attempting to fetch user from database...");
        User u = SQLService.addExistingUserByCustomerID(bank, userID);
        if (u != null && u.getCustomerID() == userID && u.getPinHash().equalsIgnoreCase(Util.hash(pin))) {
            return u;
        }
        // if we haven't found the user or have an incorrect pin, return null
        return null;
    }
    //endregion

    //region UTIL
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

    //endregion
}


