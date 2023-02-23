import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Scanner;

// Java Class containing all event/error handlers to call in main file
public class Util {

    public void checkNumbers(){
        // todo: all error handler to check for numbers only
    }
    /**
     * Adds a transaction to a given account
     * @param acc	    the account to add transaction to
     * @param amount    the $ amount of transaction
     * @param memo      memo of transaction
     */
    public static void addAcctTransaction(Account acc, double amount, String memo) {
        acc.addTransaction(amount, memo);
    }
    /**
     * Gets an internal account of a user for transferring purposes
     * by asking for user input
     * @param theUser	the user to loop through his accounts
     * @param direction direction of transfer, e.g. : transfer to / withdraw from
     * @return Account object for transferring of $
     */
    public static Account getInternalTransferAccount(User theUser, String direction, Scanner sc){
        int fromAcctIndex;
        int printSumFlag = 0;
        do {
            System.out.printf("Enter the number (1-%d) of the account to %s: ", theUser.numAccounts(), direction);
            while (printSumFlag != 1){
                theUser.printAccountsSummarySimp();
                printSumFlag +=1;
            }

            fromAcctIndex = sc.nextInt()-1;
            if (fromAcctIndex < 0 || fromAcctIndex >= theUser.numAccounts()) {
                System.out.println("Invalid account. Please try again.");
            }
        } while (fromAcctIndex < 0 || fromAcctIndex >= theUser.numAccounts());

        return theUser.getAcct(fromAcctIndex);
    }
    /**
     * Gets amount to transfer from user, with an upper transfer limit
     * @param limit the upper $ amount limit of transfer, or -1 for no limit
     * @return the amount the user inputted
     */
    public static double getTransferAmount(double limit, Scanner sc){
        double amount;
        do {
            System.out.printf("Enter the amount to transfer (max $%.02f): $",
                    limit);
            amount = sc.nextDouble();
            if (amount < 0) {
                System.out.println("Amount must be greater than zero.");
            } else if (limit != -1 && amount > limit) { // check if transferring more than limit (-1 means no limit)
                System.out.printf("Amount must not be greater than balance " +
                        "of $%.02f.\n", limit);
            }
        } while (amount < 0 || amount > limit);
        return amount;
    }
    /**
     * Gets an account by asking user for an account ID
     * @param theBank bank to loop through to look for an account
     * @return Account object
     */
    public static Account getThirdPartyTransferAccount(Bank theBank, Scanner sc){
        //get accountID to transfer to
        int toAcctIDInput;
        int toAcctID;
        Account toAcct;
        boolean accountExists = false;
        do {
            System.out.println("Enter the account number of the account to " +
                    "transfer to: ");
            sc.nextLine();
            toAcctIDInput = sc.nextInt();
            toAcctID = theBank.getAccountIndex(toAcctIDInput);
            if (toAcctID != -1){
                accountExists = true;
            }
            if (!accountExists) {
                System.out.println("Invalid account. Please try again.");
            }
        } while (!accountExists);
        // get account to transfer to with accountID
        toAcct = theBank.getAccount(toAcctID);
        return toAcct;
    }

    public static String hash(String pin){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return String.format("%032X", new BigInteger(1,md.digest(pin.getBytes())));
        } catch (Exception e) {
            System.err.println("error, caught exeption : " + e.getMessage());
            System.exit(1);
            return null;
        }
    }
}


