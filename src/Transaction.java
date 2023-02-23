import java.util.Date;

public class Transaction {

    /**
     * The amount of this transaction.
     */
    private double amount;

    /**
     * The time and date of this transaction.
     */
    private Date timestamp;

    /**
     * A memo for this transaction.
     */
    private String memo;

    /**
     * The accountID in which the transaction was made to.
     */
    private int receiverID;

    /**
     * The transaction's unique ID.
     */
    private int transactionID;

    /**
     * Create a new transaction.
     * @param amount		the dollar amount transacted
     * @param receiverID	the account the transaction belongs to
     */
    public Transaction(double amount, int receiverID) { //TODO: add transaction ID to this, but need to generate new transactionID :( mafan like 1 dog

        this.amount = amount;
        this.receiverID = receiverID;
        this.timestamp = new Date();
        this.memo = "";

    }
    public Transaction(int transactionID, int receiverID, double amount, Date timestamp, String memo){

        this.amount = amount;
        this.receiverID = receiverID;
        this.timestamp = timestamp;
        this.memo = memo;
        this.transactionID = transactionID;
    }

    /**
     * Create a new transaction with a memo.
     * @param amount	the dollar amount transacted
     * @param memo		the memo for the transaction
     * @param receiverID	the account the transaction belongs to
     */
    public Transaction(double amount, String memo, int receiverID) {

        // call the single-arg constructor first
        this(amount, receiverID);
        this.memo = memo;

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