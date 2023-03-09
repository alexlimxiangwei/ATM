import java.sql.Connection;
import java.util.Date;

public class Transaction {

    /**
     * The amount of this transaction.
     */
    private double amount;

    /**
     * The time and date of this transaction.
     */
    private java.sql.Date timestamp;

    /**
     * A memo for this transaction.
     */
    private String memo;
    /**
     * The accountID in which the transaction was made from.
     */
    private int accountID;

    public int getReceiverID() {
        return receiverID;
    }

    /**
     * The accountID in which the transaction was made to.
     */
    private int receiverID;

    /**
     * The transaction's unique ID.
     */
    private final int transactionID;

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMemo() {
        return memo;
    }

    public int getAccountID() {
        return accountID;
    }

    public int getTransactionID() {
        return transactionID;
    }

    /**
     * Create a new transaction
     * @param amount		the dollar amount transacted
     * @param accountID	    the account the transaction was made from
     * @param receiverID    the account the transaction was made to, -1 means deposit/withdraw
     * @param memo          deposit/withdraw
     */

    public Transaction(double amount, int accountID, int receiverID, String memo, Connection conn){
        this.amount = amount;
        this.timestamp = new java.sql.Date(new java.util.Date().getTime());
        this.accountID = accountID;
        // if it's a deposit/ withdrawal, no receiverID
        this.receiverID = receiverID;
        this.memo = memo;
        this.transactionID = DB_Util.generateTransactionID(conn);
        DB_Util.addTransactionToSQL(conn, this);

    }
    /**
     * Create a new transaction. This constructor is only used when importing transactions from SQL to local memory.
     * @param amount		the dollar amount transacted
     * @param receiverID	the account the transaction belongs to
     */
    public Transaction(int transactionID, int receiverID, double amount, java.sql.Date timestamp, String memo){

        this.amount = amount;
        this.receiverID = receiverID;
        this.timestamp = timestamp;
        this.memo = memo;
        this.transactionID = transactionID;
        // TODO: add all the info above as new sql entry

    }


    /**
     * Get the transaction amount.
     * @return	the amount of the transaction
     */
    public double getAmount() {
        return this.amount;
    }

    /**
     * Get a string summarizing the transaction
     * @return the summary string
     */
    public String getSummaryLine() {

        if (this.amount >= 0) {
            return String.format("%s, $%.02f : %s",
                    this.timestamp.toString(), this.amount, this.memo);
        } else {
            return String.format("%s, $(%.02f) : %s",
                    this.timestamp.toString(), -this.amount, this.memo);
        }
    }

}