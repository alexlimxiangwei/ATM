package com.CLI.ATM.ATM2.service;


import com.CLI.ATM.ATM2.CLI.AccountCLI;
import com.CLI.ATM.ATM2.Util;
import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.Bank;
import com.CLI.ATM.ATM2.model.User;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static com.CLI.ATM.ATM2.Constants.conn;

@Component
public class UserService {

    @Autowired
    BankService bankService;


    @Autowired
    AccountCLI accountCLI;

    public User createUserFromSQL(int idCustomer, String firstName, String lastName, String pin) {

        ArrayList<Account> accounts = new ArrayList<>();

        var user = new User(firstName, lastName, idCustomer, pin, accounts);

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


    /**
     * Add new user to sql database
     * @param uuid creates new account with uuid
     * @param fname creates new account with fname
     * @param lname creates new account with lname
     * @param pin creates new account with pin
     */

    public void addNewUser(int uuid, String fname, String lname, String pin, Bank bank) {
        try {
            String strUpdate = "insert into customer values(?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(strUpdate);
            stmt.setInt(1, uuid);
            stmt.setString(2, fname);
            stmt.setString(3, lname);
            stmt.setString(4, pin);
            stmt.executeUpdate();

            strUpdate = String.format("insert into Bank_has_Customer values (%d, %d %s);",bank.getBankID(),uuid, pin);
            stmt = conn.prepareStatement(strUpdate);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Changes users password
     * @param pin pin of account to update to
     * @param uuid uuid of account to update to
     */
    public void changePassword(String pin , int uuid, Bank bank){
        try {

            String hashedPassword = Util.hash(pin);
            String strSelect = "update Bank_Has_Customer set hashedPin = ? where Customer_idCustomer = ? and Bank_idBank = ?";
            PreparedStatement stmt = conn.prepareStatement(strSelect);
            stmt.setString(1,hashedPassword);
            stmt.setInt(2, uuid );
            stmt.setInt(3, bank.getBankID());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Searches SQL database for a particular account using its id
     * @param idAccount account ID to search for
     * @return true if Account exists, false otherwise
     */
    public static boolean isAccount(int idAccount){
        try {
            Statement stmt = conn.createStatement();

            String strSelect = "select * from Account where idAccount = " + idAccount;
            ResultSet rset = stmt.executeQuery(strSelect);


            if (rset.next()) {   // if there is a next row, the account exists
                return true;
            }
        }
        catch(SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Searches SQL database for a particular customerID and bank
     * also adds found user and all of its accounts and transactions to bank
     * @param bank bank to search from
     * @param idCustomer customer ID to search for
     * @return found User object or null
     */
    public User addExistingUser(Bank bank, int idCustomer){
        User user = null;
        String firstName = null;
        String lastName = null;
        String hashedPin = null;
        try {
//         Step 2: Construct a 'Statement' object called 'stmt' inside the Connection created
            Statement stmt = conn.createStatement();
            String strSelect = "select * from customer where idCustomer = " + idCustomer;
            ResultSet resultSet = stmt.executeQuery(strSelect);

            if (resultSet.next()) {   // Repeatedly process each row
                firstName = resultSet.getString("firstName");  // retrieve a 'double'-cell in the row
                lastName= resultSet.getString("lastName");
            }
            strSelect = String.format("select * from Bank_Has_Customer where Customer_idCustomer = %s and Bank_idBank = %s ", idCustomer, bank.getBankID());
            resultSet = stmt.executeQuery(strSelect);
            if (resultSet.next()) {
                hashedPin = resultSet.getString("hashedPin");
                user = bankService.addExistingUserToBank(bank, idCustomer, firstName,lastName, hashedPin);

            }

        }
        catch(SQLException ex) {
            ex.printStackTrace();
        }
        return user;
    }



}


