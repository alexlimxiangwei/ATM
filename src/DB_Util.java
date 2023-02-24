import java.sql.*;
import java.util.ArrayList;

public class DB_Util {
    public static ArrayList<Bank> fetchBanks(Connection conn){
        ArrayList<Bank> banks = new ArrayList<>();

        try {
//         Step 2: Construct a 'Statement' object called 'stmt' inside the Connection created
            Statement stmt = conn.createStatement();

            String strSelect = "select * from bank";
//            System.out.println("The SQL statement is: " + strSelect + "\n"); // Echo For debugging

            ResultSet rset = stmt.executeQuery(strSelect);

            // Step 4: Process the 'ResultSet' by scrolling the cursor forward via next().
            //  For each row, retrieve the contents of the cells with getXxx(columnName).
//            System.out.println("The records selected are:");
//            int rowCount = 0;
            // Row-cursor initially positioned before the first row of the 'ResultSet'.
            // rset.next() inside the whole-loop repeatedly moves the cursor to the next row.
            // It returns false if no more rows.
            while (rset.next()) {   // Repeatedly process each row
                int idBank = rset.getInt("idBank");  // retrieve a 'String'-cell in the row
                String name = rset.getString("name");  // retrieve a 'double'-cell in the row
                boolean local = rset.getBoolean("local");       // retrieve a 'int'-cell in the row
                banks.add(new Bank(idBank, name, local));
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

    public static User findUser(Connection conn, Bank bank, int idCustomer){
        User user = null;
        try {
//         Step 2: Construct a 'Statement' object called 'stmt' inside the Connection created
            Statement stmt = conn.createStatement();

            String strSelect = "select * from customer where idCustomer = " + idCustomer;
            ResultSet rset = stmt.executeQuery(strSelect);


            if (rset.next()) {   // Repeatedly process each row
                String firstName = rset.getString("firstName");  // retrieve a 'double'-cell in the row
                String lastName= rset.getString("lastName");
                String hashedPin = rset.getString("hashedPin");
                user = bank.addExistingUser(conn, idCustomer, firstName, lastName, hashedPin);
            }
        }
        catch(SQLException ex) {
            ex.printStackTrace();
        }
        return user;
    }
    /**
     * Fetches all existing accounts from sql for a particular user/customer and bank, and adds it to the bank
     * @param user user to search for accounts from.
     * @param bank bank to search for accounts from
     */
    public static void addAccountsToUser(Connection conn, User user, Bank bank) {
        try {
            Statement stmt = conn.createStatement();
            String strSelect = String.format("select idAccount,name,balance from Account where Customer_idCustomer = %d and Bank_idBank = %d;",user.getUUID(), bank.getBankID());
            System.out.println(strSelect);

            ResultSet rset = stmt.executeQuery(strSelect);
            while (rset.next()){
                int idAccount = rset.getInt("idAccount");
                System.out.println(idAccount);
                String name = rset.getString("name");
                double balance = rset.getDouble("balance");
                Account newAccount = new Account(idAccount, name, user, balance);
                // fetch and add all transactions for the account from sql
                //here
                DB_Util.addTransactionsToAccount(conn, newAccount);
                user.addAccount(newAccount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    /**
     * Fetches all existing transactions from sql for a particular account, and adds it to the account
     * @param acc account to search for transactions from.
     */
    public static void addTransactionsToAccount(Connection conn, Account acc){
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
                    acc.addExistingTransaction(idTransaction, receiverID, amount, date, memo);
                }
                else{
                    acc.addExistingTransaction(idTransaction, idAccount, amount * -1, date, memo);
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
    * idAccount, Bank_idBank, balance from Account
    * example sql statement: UPDATE Employees SET age=20 WHERE id=100;
    * int executeUpdate (String SQL) − Returns the number of rows affected by the execution of the SQL statement.
    * Use this method to execute SQL statements for which you expect to get a number of rows affected
    *  - for example, an INSERT, UPDATE, or DELETE statement.
    * */

    /**
     * Fetches all account balances and updates the balance of the accountId that user chooses
     * @param acc account to search for balances from.
     */
    public static void updateWithdrawals(Connection conn, Account acc) {
        try {
            String strSelect = "update account set balance = ? where idAccount =" + acc;
            PreparedStatement stmt = conn.prepareStatement(strSelect);
            stmt.setDouble(1, (acc.getBalance()));
            stmt.setInt(2, acc.getAccountID());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches all account balances and updates the balance of the accountId that user chooses
     * @param acc account to search for balances from.
     */
    public static void updateDeposits(Connection conn, Account acc) {
        try {
            String strSelect = "update account set balance = ? where idAccount =" + acc;
            PreparedStatement stmt = conn.prepareStatement(strSelect);
            stmt.setDouble(1, (acc.getBalance()));
            stmt.setInt(2, acc.getAccountID());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
