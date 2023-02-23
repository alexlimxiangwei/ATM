import java.util.ArrayList;
import java.util.Scanner;
import java.sql.*;
public class ATM {


    public static void main(String[] args) {

        // init database
        Connection conn = null;
        try {
            // Step 1: Construct a database 'Connection' object called 'conn'
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/mydb?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                    "root", "password");   // For MySQL only
            // The format is: "jdbc:mysql://hostname:port/databaseName", "username", "password"
        }
        catch(SQLException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        ArrayList<Bank> bankList = DB_Util.fetchBanks(conn); // gets all banks from database and stores it in an array

        // init Scanner
        Scanner sc = new Scanner(System.in);

        //TODO: delete all the setting up below after integrating with database

        //init Bank
        Bank currentBank = bankList.get(0); // for creating accounts in first bank, will be removed next time after testing
        // add a user, which also creates a Savings account
        User aUser = currentBank.addUser("John", "Doe", Util.hash("1234"));
//        theBank.addUser("Legoland", "Puteri", "123");
        System.out.println();

        // add a checking account for our user
        Account newAccount = new Account("Checking", aUser, currentBank, 500.00);

        aUser.addAccount(newAccount);
        currentBank.addAccount(newAccount);

        User curUser;


        // continue looping forever
        while (true) {

            System.out.println("Please select the bank you would like to use");
            for (int i = 0; i < bankList.size() ; i++){
                System.out.printf("  %d) %s\n", i + 1, bankList.get(i).getName());
            }
            System.out.print("Enter choice: ");
            int userInput = sc.nextInt();
            sc.nextLine();
            currentBank = bankList.get(userInput - 1);

            System.out.printf("\nWelcome to %s !\n", currentBank.getName());
            System.out.println("What would you like to do?");
            System.out.println("  1) Log In");
            System.out.println("  2) Sign Up");
            System.out.print("Enter choice: ");

            // stay in login prompt until successful login
            userInput = sc.nextInt();
            sc.nextLine();

            if (userInput == 1){
                // stay in login prompt until successful login
                curUser = ATM.mainMenuPrompt(conn, currentBank, sc);

                // stay in main menu until user quits
                ATM.printUserMenu(curUser, currentBank, sc);
                // stay in main menu until user quits
                ATM.printUserMenu(curUser, currentBank, sc);
            } else if (userInput == 2) {

                System.out.printf("Welcome to %s's sign up\n", currentBank.getName());

                // init class
                User newUser = new User();

                System.out.print("Enter First name: ");
                String fname = sc.nextLine();
                newUser.setFirstName(fname);

                System.out.print("Enter last name: ");
                String lname = sc.nextLine();
                newUser.setLastName(lname);

                System.out.print("Enter pin: ");
                String newPin = Util.hash(sc.nextLine()); //hash it immediately so we don't store the password at all

                // TODO: sql-ize the below
                // Creates a new user account based on user input
                User createNewUser = currentBank.addUser(fname,lname,newPin);
                Account createNewAccount = new Account("Checking", createNewUser, currentBank, 0.0);
                createNewUser.addAccount(createNewAccount);
                currentBank.addAccount(createNewAccount);

                System.out.println("Account successfully created.");
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
    public static User mainMenuPrompt(Connection conn, Bank theBank, Scanner sc) {

        // inits
        // test
        int userID;
        String pin;
        User authUser;

        // prompt user for user ID/pin combo until a correct one is reached
        do {
            System.out.print("Enter user ID: ");
            userID = sc.nextInt();
            sc.nextLine();

            System.out.print("Enter pin: ");
            pin = sc.nextLine();
            // try to get user object corresponding to ID and pin combo
            authUser = theBank.userLogin(conn, userID, pin);
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
        fromAcct = Util.getInternalTransferAccount(theUser, "transfer from", sc);
        transferLimit = fromAcct.getBalance();

        if (choice == 1){
            // get internal account to transfer to
            toAcct = Util.getInternalTransferAccount(theUser, "transfer to", sc);

        }
        else{
            //get third party account to transfer to
            toAcct = Util.getThirdPartyTransferAccount(theBank, sc);
        }
        // get amount to transfer
        amount = Util.getTransferAmount(transferLimit, sc);

        // finally, do the transfer
        Util.addAcctTransaction(fromAcct, -1*amount, String.format(
                "Transfer to account %s", fromAcct.getAccountID()));
        fromAcct.addBalance(-amount);

        Util.addAcctTransaction(toAcct, amount, String.format(
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
        fromAcct = Util.getInternalTransferAccount(theUser, "withdraw from", sc);
        withdrawLimit = fromAcct.getBalance();

        // get amount to transfer
        amount = Util.getTransferAmount(withdrawLimit, sc);

        // gobble up rest of previous input
        sc.nextLine();

        // get a memo
        System.out.print("Enter a memo: ");
        memo = sc.nextLine();

        // do the withdrawal
        Util.addAcctTransaction(fromAcct, -1*amount, memo);
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
        toAcct = Util.getInternalTransferAccount(theUser, "deposit to", sc);

        // get amount to transfer
        amount = Util.getTransferAmount(-1, sc);

        // gobble up rest of previous input
        sc.nextLine();

        // get a memo
        System.out.print("Enter a memo: ");
        memo = sc.nextLine();

        // do the deposit
        Util.addAcctTransaction(toAcct, amount, memo);
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