package com.CLI.ATM.ATM2.service;

import com.CLI.ATM.ATM2.DB_Util;
import com.CLI.ATM.ATM2.Util;
import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.Bank;
import com.CLI.ATM.ATM2.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Random;

@Component
public class BankService {

    @Autowired
    UserService userService;

    @Autowired
    AccountService accountService;

    @Autowired
    DB_Util dbUtil;

    public Bank createNewBank(int bankID, String name, boolean local) {
        var users = new ArrayList<User>();
        var accounts = new ArrayList<Account>();
        return new Bank(bankID, name, local, users, accounts);
    }

    public User addUserToBank(Bank bank, String firstName, String lastName, String pin) {

        // create a new User object and add it to our list
        User newUser = userService.createNewUser(firstName, lastName, pin, bank);
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
    public User addExistingUserToBank(Bank bank, int idCustomer, String firstName, String lastName, String pin) {

        User existingUser = userService.createUserFromSQL(idCustomer, firstName, lastName, pin);

        // adds user to banks list of users
        bank.getUsers().add(existingUser);

        //fetches all accounts and transactions belonging to user from sql
        dbUtil.addAccountsToUser(existingUser, bank);
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
            if (u.getUuid() == userID)
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
        System.out.println("User not found, attempting to fetch user from database...");
        User u = dbUtil.addExistingUser(bank, userID);
        if (u != null && u.getUuid() == userID && u.getPinHash().equalsIgnoreCase(Util.hash(pin))) {
            return u;
        }
        // if we haven't found the user or have an incorrect pin, return null
        return null;

    }

    public Account getAccountById(Bank bank, int index){
        return bank.getAccounts().get(index);
    }

    public int getAccountIndex(Bank bank, int accountID){
        for (int i = 0 ; i < bank.getAccounts().size(); i++){
            if (bank.getAccounts().get(i).getAccountID() == accountID){
                return i;
            }
        }
        return -1;
    }




    public int getNewUserUUID(Bank bank) {

        // init
        String uuid;
        Random rng = new Random();
        int len = 6;
        boolean nonUnique;

        // continue looping until we get a unique ID
        do {

            // generate the number
            uuid = "";
            for (int c = 0; c < len; c++) {
                uuid += ((Integer)rng.nextInt(10)).toString();
            }

            // check to make sure it's unique
            nonUnique = false;
            for (User u : bank.getUsers()) {
                if (uuid.compareTo(String.valueOf(u.getUuid())) == 0) {
                    nonUnique = true;
                    break;
                }
            }

        } while (nonUnique);

        return Integer.parseInt(uuid);
    }
}
