package com.CLI.ATM.ATM2.service;

import com.CLI.ATM.ATM2.Util;
import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.Bank;
import com.CLI.ATM.ATM2.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class BankService {
    @Autowired
    UserService userService;
    @Autowired
    AccountService accountService;
    @Autowired
    SQLService SQLService;

    public Bank createNewBank(int bankID, String name, boolean local) {
        var users = new ArrayList<User>();
        var accounts = new ArrayList<Account>();
        return new Bank(bankID, name, local, users, accounts);
    }

    public User addUserToBank(Bank bank, String firstName, String lastName, String pin, double local_transfer_limit, double overseas_transfer_limit) {

        // create a new User object and add it to our list
        User newUser = userService.createNewUser(firstName, lastName, pin, local_transfer_limit, overseas_transfer_limit);
        bank.getUsers().add(newUser);

        // create a savings account for the user and add it to our list
        var newAccount = accountService.createAccount("SAVING", newUser, 0.0);
        userService.addAccountToUser(newUser, newAccount);
        bank.getAccounts().add(newAccount);

        return newUser;

    }

    /**
     * Adds an existing user (that exists in sql but not in local mem) and his accounts to bank.
     * @param idCustomer Users id
     * @param firstName first name of user to add to the bank
     * @param lastName last name of user to add to the bank
     * @param pin hashed pin of the user
     * @return the created User object
     */
    public User addExistingUserToBank(Bank bank, int idCustomer, String firstName, String lastName, String pin,double local_transfer_limit, double overseas_transfer_limit) {

        User existingUser = userService.createUserFromSQL(idCustomer, firstName, lastName, pin, local_transfer_limit, overseas_transfer_limit);

        // adds user to banks list of users
        bank.getUsers().add(existingUser);

        //fetches all accounts and transactions belonging to user from sql
        SQLService.addAccountsToUser(existingUser, bank);
        // adds all users accounts to banks list of accounts
        bank.getAccounts().addAll(existingUser.getAccounts());

        return existingUser;

    }

    /**
     * Add an existing account for a particular User.
     * @param newAccount	the account
     */
    public void addAccountToBank(Bank bank, Account newAccount) {
       bank.getAccounts().add(newAccount);
    }

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
                if (userService.validatePin(u, pin)){
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

    public Account getAccountByID(Bank bank, int accountID){
        for (int i = 0 ; i < bank.getAccounts().size(); i++){
            if (bank.getAccounts().get(i).getAccountID() == accountID){
                return bank.getAccounts().get(i);
            }
        }
        return null;
    }

    public Bank getBankFromID(ArrayList<Bank> banks, int bankID){
        for (Bank bank: banks){
            if (bank.getBankID() == bankID){
                return bank;
            }
        }
        return null;
    }
}
