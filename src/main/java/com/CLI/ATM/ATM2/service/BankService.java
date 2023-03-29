package com.CLI.ATM.ATM2.service;

import com.CLI.ATM.ATM2.Util;
import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.Bank;
import com.CLI.ATM.ATM2.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static com.CLI.ATM.ATM2.Constants.bankList;

@Component
public class BankService {
    @Autowired
    UserService userService;
    @Autowired
    AccountService accountService;
    @Autowired
    SQLService SQLService;

    //region BANK_CREATION
    /**
     * Creates new bank.
     * @param bankID bankID for the bank
     * @param name name of the bank
     * @param local to specify if it is local or overseas
     */
    public Bank createNewBank(int bankID, String name, boolean local) {
        var users = new ArrayList<User>();
        var accounts = new ArrayList<Account>();
        return new Bank(bankID, name, local, users, accounts);
    }
    //endregion

    //region ADD_USER_&_ACC_TO_BANK
    /**
     * Adds new user to bank
     * @param bank                  the current bank that is chosen
     * @param firstName             name of the bank
     * @param lastName              to specify if it is local or overseas
     * @param pin                   adds user-created pin to the bank
     * @param local_transfer_limit  adds local transfer limit to the user
     * @param overseas_transfer_limit adds overseas transfer limit to the user
     * @return                       the created newUser object
     */
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
     * @param bank the current bank that is chosen
     * @param idCustomer Users id
     * @param firstName first name of user to add to the bank
     * @param lastName last name of user to add to the bank
     * @param pin hashed pin of the user
     * @param local_transfer_limit adds local transfer limit to the user
     * @param overseas_transfer_limit adds overseas transfer limit to the user
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
    //endregion

    //region USER_LOGIN

    //endregion


    //region GETTERS
    /**
     * gets the accountID requested
     * @param bank	the bank that the user is in
     * @param accountID gets the accountID
     */
    public Account getAccountFromID(Bank bank, int accountID){
        for (int i = 0 ; i < bank.getAccounts().size(); i++){
            if (bank.getAccounts().get(i).getAccountID() == accountID){
                return bank.getAccounts().get(i);
            }
        }
        return null;
    }

    /**
     * gets the bankID requested
     * @param banks	finds the bank requested from the arraylist
     * @param bankID gets the bankID
     */
    public Bank getBankFromID(ArrayList<Bank> banks, int bankID){
        for (Bank bank: banks){
            if (bank.getBankID() == bankID){
                return bank;
            }
        }
        return null;
    }

    /**
     * gets a User from his ID
     * @param bank	the bank that the user is in
     * @param customerID gets the accountID
     * @return User object found or null if not found
     */
    public User getUserFromID(Bank bank, int customerID){
        for (int i = 0 ; i < bank.getUsers().size(); i++){
            if (bank.getUsers().get(i).getCustomerID() == customerID){
                return bank.getUsers().get(i);
            }
        }
        return null;
    }

    /**
     * gets the number of local banks
     * @return number of local banks
     */
    public int getNumOfLocalBanks() {
        int num_of_local_banks = 0;
        for (Bank bank : bankList) {
            if (bank.isLocal()){
                num_of_local_banks++;
            }
        }
        return num_of_local_banks;
    }

    //endregion
}
