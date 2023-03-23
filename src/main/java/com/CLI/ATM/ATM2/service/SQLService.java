package com.CLI.ATM.ATM2.service;

import com.CLI.ATM.ATM2.Util;
import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.Bank;

import com.CLI.ATM.ATM2.model.Transaction;
import com.CLI.ATM.ATM2.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;

import static com.CLI.ATM.ATM2.Constants.bankList;
import static com.CLI.ATM.ATM2.Constants.conn;


@Component
public class SQLService {
    //region INIT
    @Autowired
    BankService bankService;
    @Autowired
    UserService userService;
    @Autowired
    AccountService accountService;

    public void initSQLConnection(){
        try {
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/mydb?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                    "root", "");
            // The format is: "jdbc:mysql://hostname:port/databaseName", "username", "password"
        }
        catch(SQLException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    //endregion

    //region BANK FUNCTIONS

    /**
     * Fetches all banks that exist on SQL
     * @return an ArrayList of Banks
     */
    public ArrayList<Bank> fetchBanks(){
        ArrayList<Bank> banks = new ArrayList<>();

        try {
            Statement stmt = conn.createStatement();

            String strSelect = "select * from bank";

            ResultSet resultSet = stmt.executeQuery(strSelect);

            while (resultSet.next()) {   // Repeatedly process each row
                int idBank = resultSet.getInt("idBank");  // retrieve a 'String'-cell in the row
                String name = resultSet.getString("name");  // retrieve a 'double'-cell in the row
                boolean local = resultSet.getBoolean("local");       // retrieve a 'int'-cell in the row
                Bank newBank = bankService.createNewBank(idBank, name, local);
                banks.add(newBank);
            }
        }
        catch(SQLException ex) {
            ex.printStackTrace();
        }
        return banks;
    }

    //endregion

    //region ACCOUNT FUNCTIONS

    /**
     * Add new account to sql database
     * @param idAcc creates new account with uuid
     * @param cust_has_id_customer gets the customerID
     * @param bank_id_bank gets the bankID
     * @param name creates new account with pin
     */
    public void addAccount(int idAcc, int cust_has_id_customer, int bank_id_bank, String name, double bal) {
        try {
            String strUpdate = "insert into account values(?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(strUpdate);
            stmt.setInt(1, idAcc);
            stmt.setInt(2, cust_has_id_customer);
            stmt.setInt(3, bank_id_bank);
            stmt.setString(4, name);
            stmt.setDouble(5,bal);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches all existing transactions from sql for a particular account, and adds it to the account
     * @param acc account to search for transactions from.
     */
    public void addAllTransactionsToAccount(Account acc){
        try {
            Statement stmt = conn.createStatement();
            int id = acc.getAccountID();
            String strSelect = String.format("select * from Transaction where Account_idAccount =" +
                    " %d or receiverID = %d order by idTransaction;", id, id);
            ResultSet rset = stmt.executeQuery(strSelect);
            while (rset.next()){
                int idTransaction = rset.getInt("idTransaction");
                int idAccount = rset.getInt("Account_idAccount");
                int receiverID = rset.getInt("receiverID");
                Date date = rset.getDate("timeStamp");
                double amount = rset.getDouble("amount");
                String memo = rset.getString("memo");

                if (idAccount == id) {
                    accountService.addExistingTransaction(acc, idTransaction, receiverID, amount, date, memo);
                }
                else{
                    accountService.addExistingTransaction(acc, idTransaction, receiverID, amount * -1, date, memo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * Fetches all existing accounts from sql for a particular user/customer and bank, and adds it to the bank
     * Called when user is found in DB_Util.findUser()
     * @param user user to search for accounts from.
     * @param bank bank to search for accounts from.
     */
    public void addAccountsToUser(User user, Bank bank) {

        try {
            Statement stmt = conn.createStatement();
            String strSelect = String.format("select idAccount,name,balance from Account where Customer_idCustomer = %d and Bank_idBank = %d;",user.getCustomerID(), bank.getBankID());

            ResultSet rset = stmt.executeQuery(strSelect);
            while (rset.next()){
                int idAccount = rset.getInt("idAccount");
                String name = rset.getString("name");
                double balance = rset.getDouble("balance");
                Account newAccount = accountService.importAccountFromSQL(idAccount, name, user, balance);

                // fetch and add all transactions for the account from sql
                addAllTransactionsToAccount(newAccount);
                userService.addAccountToUser(user, newAccount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Fetches all account balances and updates the balance of the accountId that user chooses
     * @param amount amount to set balance of account to
     * @param acctID ID of account to update balance of
     */
    public void updateBalance(double amount, int acctID) {
        try {
            //first get current balance
            double balance = 0;
            String strSelect = String.format("select * from Account where idAccount = %d;", acctID);
            PreparedStatement stmt = conn.prepareStatement(strSelect);
            ResultSet rset = stmt.executeQuery(strSelect);
            if (rset.next()){
                balance = rset.getDouble("balance");
            }
            balance += amount;
            String strUpdate = "update account set balance = ? where idAccount = ?";
            stmt = conn.prepareStatement(strUpdate);
            stmt.setDouble(1, balance);
            stmt.setInt(2, acctID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Deletes an account from SQL database
     * @param idAcc id of the account to delete
     */
    public void deleteAccount(int idAcc){ //int cust_has_id_customer
        try{
            String strUpdate = "delete from Transaction where Account_idAccount = ?;"; //Customer_idCustomer = ? AND
            PreparedStatement stmt = conn.prepareStatement(strUpdate);
            stmt.setInt(1, idAcc);
            stmt.executeUpdate();

            strUpdate = "delete from account where idAccount = ?;"; //Customer_idCustomer = ? AND
            stmt = conn.prepareStatement(strUpdate);
            stmt.setInt(1, idAcc);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Update user's new account name on sql database
     * @param idAcc id of account
     * @param cust_has_id_customer id of customer
     * @param name the name the user wants to change to
     */
    public void changeAccountName(int idAcc, int cust_has_id_customer, String name){
        try{
            String strSelect = "update account set name = ? where Customer_idCustomer = ? AND idAccount = ?";
            PreparedStatement stmt = conn.prepareStatement(strSelect);
            stmt.setString(1, name);
            stmt.setInt(2, idAcc);
            stmt.setInt(3, cust_has_id_customer);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Gets the biggest transaction number from SQL server, and returns +1 of it
     */
    public int generateNewAccountID(){
        int max_id = 0;
        try {
            String strSelect = "select idAccount from Account order by idAccount desc limit 1;";
            PreparedStatement stmt = conn.prepareStatement(strSelect);
            ResultSet rset = stmt.executeQuery(strSelect);
            rset.next();
            max_id = rset.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return max_id + 1;
    }



    //endregion

    //region CUSTOMER FUNCTIONS

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
     * Searches SQL database for a particular customerID and bank
     * also adds found user and all of its accounts and transactions to bank
     * @param bank bank to search from
     * @param idCustomer customer ID to search for
     * @return found User object or null
     */
    public User addExistingUserByCustomerID(Bank bank, int idCustomer){
        User user = null;
        String firstName = null;
        String lastName = null;
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
                String hashedPin = resultSet.getString("hashedPin");
                double local_transfer_limit = resultSet.getDouble("local_transfer_limit");
                double overseas_transfer_limit = resultSet.getDouble("overseas_transfer_limit");
                user = bankService.addExistingUserToBank(bank, idCustomer, firstName,lastName, hashedPin, local_transfer_limit, overseas_transfer_limit);

            }

        }
        catch(SQLException ex) {
            ex.printStackTrace();
        }
        return user;
    }


    /**
     * Searches SQL database for a particular User based on only account ID
     * adds found user and all of its accounts and transactions to bank
     *
     * @param idAccount adds User based on only account ID
     */
    public void addExistingUserByAcctId(int idAccount){
        try {
            Statement stmt = conn.createStatement();

            String strSelect = "select * from Account where idAccount = " + idAccount;
            ResultSet resultSet = stmt.executeQuery(strSelect);

            if (resultSet.next()) {   // if there is a next row, the account exists
                int idBank = resultSet.getInt("Bank_idBank");
                int idCustomer = resultSet.getInt("Customer_idCustomer");
                addExistingUserByCustomerID(bankService.getBankFromID(bankList, idBank), idCustomer);
            }
        }
        catch(SQLException ex) {
            ex.printStackTrace();
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
    public boolean isSQLAccount(int idAccount){
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
     * Gets the biggest User id from SQL server, and returns +1 of it
     */
    public int generateNewCustomerID(){
        int max_id = 0;
        try {
            String strSelect = "select idCustomer from Customer order by idACustomer desc limit 1;";
            PreparedStatement stmt = conn.prepareStatement(strSelect);
            ResultSet rset = stmt.executeQuery(strSelect);
            rset.next();
            max_id = rset.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return max_id + 1;
    }


    //endregion

    //region TRANSACTION FUNCTIONS

    /**
     * Adds existing local Transaction to SQL
     * @param txn existing transaction object
     */
    public void addTransaction(Transaction txn) {
        try {
            String strSelect = "insert into Transaction values(?, ?, ?, ? , ? , ?)";

            PreparedStatement stmt = conn.prepareStatement(strSelect);
            stmt.setInt(1, txn.getTransactionID());
            stmt.setInt(2, txn.getAccountID());
            stmt.setDouble(3,txn.getAmount());
            stmt.setDate(4, txn.getTimestamp());
            stmt.setInt(5, txn.getReceiverID());
            stmt.setString(6, txn.getMemo());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the biggest transaction number from SQL server, and returns +1 of it
     */
    public int generateTransactionID(){
        int max_id = 0;
        try {
            String strSelect = "select idTransaction from Transaction order by idTransaction desc limit 1;";
            PreparedStatement stmt = conn.prepareStatement(strSelect);
            ResultSet rset = stmt.executeQuery(strSelect);
            rset.next();
            max_id = rset.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return max_id + 1;
    }

    //endregion
}
