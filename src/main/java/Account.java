
import java.util.ArrayList;
import java.util.HashMap;

public class Account {

    /**
     * The name of the account.
     */
    private String name;

    /**
     * The account ID number.
     */
    private String accountID;

    /**
     * The User object that owns this account.
     */
    private User holder;

    /**
     * The list of transactions for this account.
     */
    private ArrayList<Transaction> transactions;

    /**
     * The account balance
     */
    private Double balance;



    //TODO: add account balance field, and its getters/setters
    //TODO: add withdrawal / transfer limits, and its getters/setters
    //TODO: optional , overseas withdrawal limit (must also add bank country in Bank.java)



    /**
     * Create new Account instance
     * @param name		the name of the account
     * @param holder	the User object that holds this account
     * @param theBank	the bank that issues the account
     */
    public Account(String name, User holder, Bank theBank, Double balance) {

        // set the account name and holder
        this.name = name;
        this.holder = holder;

        // get next account UUID
        this.accountID = theBank.getNewAccountID();

        // init transactions
        this.transactions = new ArrayList<Transaction>();

        this.balance = balance;
    }

    /**
     * Get the account number.
     * @return	the accountID
     */
    public String getAccountID() {
        return this.accountID;
    }

    /**
     * Get the account balance.
     * @return	account balance
     */

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
    public void addBalance(double amount){
        this.balance += amount;
    }

    /**
     * Add a new transaction in this account.
     * @param amount the amount transacted
     */
    public void addTransaction(double amount) {

        // create new transaction and add it to our list
        Transaction newTrans = new Transaction(amount, this);
        this.transactions.add(newTrans);

    }

    /**
     * Add a new transaction in this account.
     * @param amount	the amount transacted
     * @param memo		the transaction memo
     */
    public void addTransaction(double amount, String memo) {

        // create new transaction and add it to our list
        Transaction newTrans = new Transaction(amount, memo, this);
        this.transactions.add(newTrans);

    }


    /**
     * Get summary line for account
     * @return	the summary line
     */
    public HashMap<String,String> getSummaryLine() {

        // get the account's balance
        double balance = this.getBalance();

        // summary value
        HashMap<String, String> val = new HashMap<String, String>();
        // format summary line depending on whether balance is negative

            val.put("balance", String.format("%.2f", balance));
            val.put("uuid", this.accountID);
            val.put("type", this.name);

            return val;



    }

    /**
     * Print transaction history for account
     */
    //TODO : make this filo I/O to store all transactions
    public void printTransHistory() {

        System.out.printf("\nTransaction history for account %s\n", this.accountID);
        for (int t = this.transactions.size()-1; t >= 0; t--) {
            System.out.println(this.transactions.get(t).getSummaryLine());
        }
        System.out.println();

    }

}