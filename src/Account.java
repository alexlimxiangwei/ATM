
import java.util.ArrayList;

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

    private Double accountBal;

    /**
     * The account balance
     */

    //TODO: add account balance field, and its getters/setters
    //TODO: add withdrawal / transfer limits, and its getters/setters
    //TODO: optional , overseas withdrawal limit (must also add bank country in Bank.java)



    /**
     * Create new Account instance
     * @param name		the name of the account
     * @param holder	the User object that holds this account
     * @param theBank	the bank that issues the account
     */
    public Account(String name, User holder, Bank theBank, Double accountBal) {

        // set the account name and holder
        this.name = name;
        this.holder = holder;

        // get next account UUID
        this.accountID = theBank.getNewAccountID();

        // init transactions
        this.transactions = new ArrayList<Transaction>();

        this.accountBal = accountBal;
    }

    /**
     * Get the account number.
     * @return	the uuid
     */
    public String getAccountID() {
        return this.accountID;
    }

    /**
     * Add a new transaction in this account.
     * @param amount the amount transacted
     */

    public Double getAccountBal(String uuid) {
        return accountBal;
    }

    public void setAccountBal(Double accountBal) {
        this.accountBal = accountBal;
    }

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
     * Get the balance of this account by adding the amounts of the
     * transactions.
     * @return	the balance value
     */
    public double getBalance() { // TODO: the current code doesnt have account balance, which means we need to
        //TODO:  calculate the balance everytime we need to get the balance. Not good if we have thousands of txsactions

        double balance = 0;
        double accountBal = 0;
        for (Transaction t : this.transactions) {
            balance += t.getAmount();
        }
        return balance;

    }

    /**
     * Get summary line for account
     * @return	the summary line
     */
    public String getSummaryLine() {

        // get the account's balance
        double balance = this.getBalance();

        // format summary line depending on whether balance is negative
        if (balance >= 0) {
            return String.format("%s : $%.02f : %s", this.accountID, balance,
                    this.name);
        } else {
            return String.format("%s : $(%.02f) : %s", this.accountID, balance, //TODO: dont think we should allow negative balance
                    this.name);
        }

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