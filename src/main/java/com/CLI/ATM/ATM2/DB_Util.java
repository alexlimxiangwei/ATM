package com.CLI.ATM.ATM2;

import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.Bank;
import com.CLI.ATM.ATM2.model.Transaction;
import com.CLI.ATM.ATM2.model.User;
import com.CLI.ATM.ATM2.service.AccountService;
import com.CLI.ATM.ATM2.service.BankService;
import com.CLI.ATM.ATM2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;

import static com.CLI.ATM.ATM2.Constants.conn;

@Component
public class DB_Util {

    /**
     * Fetches banks from SQL database and adds them to a list of Bank objects
     * *@return ArrayList of Banks
     */
    @Autowired
    UserService userService;

    @Autowired
    BankService bankService;

    @Autowired
    AccountService accountService;

    @Autowired
    DB_Util dbUtil;

    public  ArrayList<Bank> fetchBanks(){
        ArrayList<Bank> banks = new ArrayList<>();

        try {
//         Step 2: Construct a 'Statement' object called 'stmt' inside the Connection created
            Statement stmt = conn.createStatement();

            String strSelect = "select * from bank";
//            System.out.println("The SQL statement is: " + strSelect + "\n"); // Echo For debugging

            ResultSet resultSet = stmt.executeQuery(strSelect);

            // Step 4: Process the 'ResultSet' by scrolling the cursor forward via next().
            //  For each row, retrieve the contents of the cells with getXxx(columnName).
//            System.out.println("The records selected are:");
//            int rowCount = 0;
            // Row-cursor initially positioned before the first row of the 'ResultSet'.
            // resultSet.next() inside the whole-loop repeatedly moves the cursor to the next row.
            // It returns false if no more rows.
            while (resultSet.next()) {   // Repeatedly process each row
                int idBank = resultSet.getInt("idBank");  // retrieve a 'String'-cell in the row
                String name = resultSet.getString("name");  // retrieve a 'double'-cell in the row
                boolean local = resultSet.getBoolean("local");       // retrieve a 'int'-cell in the row
                Bank newBank = bankService.createNewBank(idBank, name, local);
                banks.add(newBank);
//                System.out.println(idBank + ", " + name + ", " + local);
//                ++rowCount;
            }
//            System.out.println("Total number of records = " + rowCount);
        }
         catch(SQLException ex) {
            ex.printStackTrace();
        }
        return banks;
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
        try {
//         Step 2: Construct a 'Statement' object called 'stmt' inside the Connection created
            Statement stmt = conn.createStatement();
            String strSelect = "select * from customer where idCustomer = " + idCustomer;
            System.out.println(strSelect);
            ResultSet resultSet = stmt.executeQuery(strSelect);

            if (resultSet.next()) {   // Repeatedly process each row
                String firstName = resultSet.getString("firstName");  // retrieve a 'double'-cell in the row
                String lastName= resultSet.getString("lastName");
                String hashedPin = resultSet.getString("hashedPin");
                user = bankService.addExistingUserToBank(bank, idCustomer, firstName,lastName, hashedPin);
            }
        }
        catch(SQLException ex) {
            ex.printStackTrace();
        }
        return user;
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
     * Fetches all existing accounts from sql for a particular user/customer and bank, and adds it to the bank
     * Called when user is found in DB_Util.findUser()
     * @param user user to search for accounts from.
     * @param bank bank to search for accounts from.
     */
    public void addAccountsToUser(User user, Bank bank) {

        try {
            Statement stmt = conn.createStatement();
            String strSelect = String.format("select idAccount,name,balance from Account where Customer_idCustomer = %d and Bank_idBank = %d;",user.getUuid(), bank.getBankID());

            ResultSet rset = stmt.executeQuery(strSelect);
            while (rset.next()){
                int idAccount = rset.getInt("idAccount");
                System.out.println(idAccount);
                String name = rset.getString("name");
                double balance = rset.getDouble("balance");
                Account newAccount = accountService.createAccountExistingId(idAccount, name, user, balance);
                // fetch and add all transactions for the account from sql
                //here
                dbUtil.addAllTransactionsToAccount(newAccount);
                userService.addAccountToUser(user, newAccount);
            }
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

    public void addTransactionToSQL(Transaction txn) {
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
     * Fetches all account balances and updates the balance of the accountId that user chooses
     * @param amount amount to set balance of account to
     * @param AcctID ID of account to update balance of
     */
    public void updateSQLBalance(double amount, int AcctID) {
        try {
            String strSelect = "update account set balance = ? where idAccount = ?";
            PreparedStatement stmt = conn.prepareStatement(strSelect);
            stmt.setDouble(1, amount);
            stmt.setInt(2, AcctID);
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
    public void changePassword(String pin , int uuid){
        try {

            String hashedPassword = Util.hash(pin);
            String strSelect = "update customer set hashedPin = ? where idCustomer = ?";
            PreparedStatement stmt = conn.prepareStatement(strSelect);
            stmt.setString(1,hashedPassword);
            stmt.setInt(2, uuid );
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add new user to sql database
     * @param uuid creates new account with uuid
     * @param fname creates new account with fname
     * @param lname creates new account with lname
     * @param pin creates new account with pin
     */

    public void addNewUser(int uuid, String fname, String lname, String pin) {
        try {
            String strSelect = "insert into customer values(?, ?, ?, ? )";
            PreparedStatement stmt = conn.prepareStatement(strSelect);
            stmt.setInt(1, uuid);
            stmt.setString(2, fname);
            stmt.setString(3, lname);
            stmt.setString(4, pin);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


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

            System.out.println(stmt);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update user's new account name to sql database
     * @param idAcc get the uuid
     * @param cust_has_id_customer gets the customerID
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

    /*
     * delete user's specified account from sql database
     * idAcc creates new account with uuid
     * cust_has_id_customer creates new account with fname
     * bank_id_bank creates new account with lname
     * name creates new account with pin
     */

    public void deleteAccount(int idAcc){ //int cust_has_id_customer
        try{
            String strSelect = "delete from account where idAccount = ?"; //Customer_idCustomer = ? AND
            PreparedStatement stmt = conn.prepareStatement(strSelect);
            stmt.setInt(1, idAcc);
            //stmt.setInt(2, cust_has_id_customer);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
