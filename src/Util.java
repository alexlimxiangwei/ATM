import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Scanner;

// Java Class containing all event/error handlers to call in main file
public class Util {

    public void checkNumbers(){
        // todo: all error handler to check for numbers only
    }

    /**
     * Gets an internal account of a user for transferring purposes
     * by asking for user input
     * @param theUser	the user to loop through his accounts
     * @param directionString direction of transfer, e.g. : transfer to / withdraw from
     * @return Account object for transferring of $
     */
    public static Account getInternalTransferAccount(User theUser, String directionString, Scanner sc){
        int fromAcctIndex;
        int printSumFlag = 0;
        do {
            System.out.printf("Enter the number (1-%d) of the account to %s: ", theUser.numAccounts(), directionString);
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
            if (limit == -1){
                System.out.print("Enter the amount to transfer: $");
            }
            else{
                System.out.printf("Enter the amount to transfer (max $%.02f): $",
                        limit);
            }
            amount = sc.nextDouble();
            if (amount < 0) {
                System.out.println("Amount must be greater than zero.");
            } else if (limit != -1 && amount > limit) { // check if transferring more than limit (-1 means no limit)
                System.out.printf("Amount must not be greater than balance " +
                        "of $%.02f.\n", limit);
            }
        } while (amount < 0 || (limit != -1 && amount > limit));
        return amount;
    }
    /**
     * Gets an account by asking user for an account ID
     * @param banks ArrayList of banks to loop through to look for an account
     * @return found accountid and found bankID, bankID is -1 if not in local memory
     */
    public static int[] getThirdPartyTransferAccount(ArrayList<Bank> banks, Scanner sc){
        //get accountID to transfer to
        int toAcctIDInput;
        int toAcctID;
        boolean accountExists = false;
        int bankID = -1;
        do {
            System.out.println("Enter the account number of the account to " +
                    "transfer to: ");
            sc.nextLine();
            toAcctIDInput = sc.nextInt();
            //look in every bank to find the account
            for (int i = 0; i < banks.size() ; i++) {
                toAcctID = banks.get(i).getAccountIndex(toAcctIDInput);
                if (toAcctID != -1){ // if account is found:
                    accountExists = true;
                    bankID = i;
                    break;
                }
            }

            // if account doesnt exist in local memory, as well as in sql database,
            if (!accountExists && !DB_Util.isAccount(toAcctIDInput)) {
                // invalid account
                System.out.println("Invalid account. Please try again.");
            }
        } while (!accountExists);
        // get accountId and which bank it belongs to if it exists in local mem
        return new int[] {toAcctIDInput, bankID};
    }

    public static String hash(String pin){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return String.format("%064X", new BigInteger(1,md.digest(pin.getBytes())));
        } catch (Exception e) {
            System.err.println("error, caught exception : " + e.getMessage());
            System.exit(1);
            return null;
        }
    }
}


