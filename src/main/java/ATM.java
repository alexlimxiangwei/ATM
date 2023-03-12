import java.util.Scanner;

public class ATM {


    public static void main(String[] args) {

        // init Scanner
        Scanner sc = new Scanner(System.in);

        //TODO: accounts, transactions and bank data should be stored in a file, and main should read the file to generate all previous data.
        //TODO: all new accounts and transaction data should be written into file ofc
        // init Bank
        Bank theBank = new Bank("Bank of Drausin");

        // add a user, which also creates a Savings account
        User aUser = theBank.addUser("John", "Doe", "1234");
        theBank.addUser("Legoland", "Puteri", "123");

        // add a checking account for our user
        Account newAccount = new Account("Checking", aUser, theBank, 500.00);
        aUser.addAccount(newAccount);
        theBank.addAccount(newAccount);


        User curUser;


        // continue looping forever
        while (true) {
            System.out.print("Please press 1 to login or 2 to to sign up ");

            // stay in login prompt until successful login
            int userInput = sc.nextInt();

            if (userInput == 1){
                // stay in login prompt until successful login
                curUser = ATM.mainMenuPrompt(theBank, sc);

                // stay in main menu until user quits
                ATM.printUserMenu(curUser, theBank, sc);
                // stay in main menu until user quits
                ATM.printUserMenu(curUser, theBank, sc);
            } else if (userInput == 2) {
                System.out.println("You are on sign up landing");
            } else {
                System.out.println("You have entered invalid number");
            }


        }

    }

    /**
     * Print the ATM's login menu.
     * @param theBank	the Bank object whose accounts to use
     * @param sc		the Scanner objec to use for user input
     */
    public static User mainMenuPrompt(Bank theBank, Scanner sc) {

        // inits
        // test
        String userID;
        String pin;
        User authUser;

        // prompt user for user ID/pin combo until a correct one is reached
        do {

            System.out.printf("\n\nWelcome to %s\n\n", theBank.getName()); //TODO: maybe we can have multiple banks
            System.out.print("Enter user ID: ");
            userID = sc.nextLine();
            sc.nextLine();
            System.out.print("Enter pin: ");
            pin = sc.nextLine();

            // try to get user object corresponding to ID and pin combo
            authUser = theBank.userLogin(userID, pin);
            if (authUser == null) {
                System.out.println("Incorrect user ID/pin combination. " +
                        "Please try again");
            }

        } while(authUser == null); 	// continue looping until we have a
        // successful login

        return authUser;

    }

    /**
     * Print the ATM's menu for user actions.
     * @param theUser	the logged-in User object
     * @param sc		the Scanner objec to use for user input
     */
    public static void printUserMenu(User theUser, Bank theBank, Scanner sc) {

        // print a summary of the user's accounts
        theUser.printAccountsSummary();

        // init
        int choice;

        // user menu
        do {

            System.out.println("What would you like to do?");
            System.out.println("  1) Show account transaction history");
            System.out.println("  2) Withdraw");
            System.out.println("  3) Deposit");
            System.out.println("  4) Transfer");
            System.out.println("  5) Change Password");
            System.out.println("  6) Quit"); // TODO: Account balance check, password change/reset, settings
            System.out.println();
            System.out.print("Enter choice: ");
            choice = sc.nextInt();

            if (choice < 1 || choice > 6) {
                System.out.println("Invalid choice. Please choose 1-5.");
            }

        } while (choice < 1 || choice > 6);

        // process the choice
        switch (choice) {
            case 1 -> ATM.showTransHistory(theUser, sc);
            case 2 -> ATM.withdrawFunds(theUser, sc);
            case 3 -> ATM.depositFunds(theUser, sc);
            case 4 -> ATM.transferFunds(theUser,theBank, sc);
            case 5 -> ATM.changePassword(theUser, sc);
            case 6 -> sc.nextLine(); // gobble up rest of previous input
        }

        // redisplay this menu unless the user wants to quit
        if (choice != 6) {
            ATM.printUserMenu(theUser,theBank, sc);
        }

    }

    /**
     * Process transferring funds from one account to another.
     * @param theUser	the logged-in User object
     * @param sc		the Scanner object used for user input
     */
    public static void transferFunds(User theUser,Bank theBank, Scanner sc) {
        // TODO: fundamental feature : inter account transfer / third-party transfer
        //TODO: seems to only have inter account transfer for now, cant xfer to other users
        Account fromAcct;
        Account toAcct;
        double amount;
        double transferLimit;

        int choice = 1;
        do {
            System.out.println("Enter a choice below: ");
            System.out.println("1) Inter-account transfer");
            System.out.println("2) Third party transfer");
            choice = sc.nextInt();

        } while(choice < 0 || choice > 2);
        // get account to transfer from
        fromAcct = Utils.getInternalTransferAccount(theUser, "transfer from", sc);
        transferLimit = fromAcct.getBalance();

        if (choice == 1){
            // get internal account to transfer to
            toAcct = Utils.getInternalTransferAccount(theUser, "transfer to", sc);

        }
        else{
            //get third party account to transfer to
            toAcct = Utils.getThirdPartyTransferAccount(theBank, sc);
        }
        // get amount to transfer
        amount = Utils.getTransferAmount(transferLimit, sc);

        // finally, do the transfer
        Utils.addAcctTransaction(fromAcct, -1*amount, String.format(
                "Transfer to account %s", fromAcct.getAccountID()));
        fromAcct.addBalance(-amount);

        Utils.addAcctTransaction(toAcct, amount, String.format(
                "Transfer from account %s", toAcct));
        fromAcct.addBalance(amount);
    }

    /**
     * Process a fund withdraw from an account.
     * @param theUser	the logged-in User object
     * @param sc		the Scanner object used for user input
     */
    public static void withdrawFunds(User theUser, Scanner sc) {

        Account fromAcct;
        double amount;
        double withdrawLimit;
        String memo;

        // get account to withdraw from
        fromAcct = Utils.getInternalTransferAccount(theUser, "withdraw from", sc);
        withdrawLimit = fromAcct.getBalance();

        // get amount to transfer
        amount = Utils.getTransferAmount(withdrawLimit, sc);

        // gobble up rest of previous input
        sc.nextLine();

        // get a memo
        System.out.print("Enter a memo: ");
        memo = sc.nextLine();

        // do the withdrawal
        Utils.addAcctTransaction(fromAcct, -1*amount, memo);
        fromAcct.addBalance(-amount);
    }

    /**
     * Process a fund deposit to an account.
     * @param theUser	the logged-in User object
     * @param sc		the Scanner object used for user input
     */
    public static void depositFunds(User theUser, Scanner sc) {

        Account toAcct;
        double amount;
        String memo;

        // get account to deposit from
        toAcct = Utils.getInternalTransferAccount(theUser, "deposit to", sc);

        // get amount to transfer
        amount = Utils.getTransferAmount(-1, sc);

        // gobble up rest of previous input
        sc.nextLine();

        // get a memo
        System.out.print("Enter a memo: ");
        memo = sc.nextLine();

        // do the deposit
        Utils.addAcctTransaction(toAcct, amount, memo);
        toAcct.addBalance(amount);
    }

    /**
     * Show the transaction history for an account.
     * @param theUser	the logged-in User object
     * @param sc		the Scanner object used for user input
     */
    public static void showTransHistory(User theUser, Scanner sc) {

        int theAcct;

        // get account whose transactions to print
        do {
            System.out.printf("Enter the number (1-%d) of the account\nwhose " +
                    "transactions you want to see: ", theUser.numAccounts());


            theAcct = sc.nextInt()-1;
            if (theAcct < 0 || theAcct >= theUser.numAccounts()) {
                System.out.println("Invalid account. Please try again.");
            }
        } while (theAcct < 0 || theAcct >= theUser.numAccounts());

        // print the transaction history
        theUser.printAcctTransHistory(theAcct);

    }
    //change passwords
    public static void changePassword(User theUser, Scanner sc) {
        String pin;
        boolean is_validated;
        sc.nextLine(); // remove empty line first
        do {
            System.out.println("Enter current password: ");
            pin = sc.nextLine();
            is_validated = theUser.validatePin(pin);
            if (!is_validated) {
                System.out.println("Incorrect password, please try again. ");
            }
        }while(!is_validated);
        is_validated = false;
        do {
            System.out.println("Enter new password: ");
            pin = sc.nextLine();
            System.out.println("Confirm new password: ");
            String confirm_Pin = sc.nextLine();
            is_validated = pin.equals(confirm_Pin);
            if (!is_validated) {
                System.out.println("The two passwords do not match, please try again.");

            }
        }while(!is_validated);
        theUser.setPin(pin);
        System.out.println("Password successfully changed.");


    }
    //TODO: add unit testing function(s) below

}