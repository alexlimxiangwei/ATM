import java.util.ArrayList;
import java.util.Scanner;
import java.sql.*;

public class ATM {
    static final int DEPOSIT = 1;
    static final int WITHDRAW = -1;

    public static Connection conn;

    public static void main(String[] args) {



        try {
            // Step 1: Construct a database 'Connection' object called 'conn'
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/mydb?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                    "root", "");   // For MySQL only
            // The format is: "jdbc:mysql://hostname:port/databaseName", "username", "password"
        }
        catch(SQLException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        ArrayList<Bank> bankList = DB_Util.fetchBanks(); // gets all banks from database and stores it in an array

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
                curUser = ATM.mainMenuPrompt(currentBank, sc);

                // stay in main menu until user quits
                ATM.printUserMenu(bankList, curUser, currentBank, sc);

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

                // Add new user to SQL
                DB_Util.addNewUser(createNewUser.getUUID(),fname,lname,newPin);

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
    public static void printUserMenu(ArrayList<Bank> bankList, User theUser, Bank theBank, Scanner sc) {

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
            System.out.println("  6) Change account name");
            System.out.println("  7) Quit"); // TODO: Account balance check, password change/reset, settings
            System.out.println();
            System.out.print("Enter choice: ");
            choice = sc.nextInt();

            if (choice < 1 || choice > 7) {
                System.out.println("Invalid choice. Please choose 1-7.");
            }

        } while (choice < 1 || choice > 7);

        // process the choice
        switch (choice) {
            case 1 -> ATM.showTransHistory(theUser, sc);
            case 2 -> ATM.updateFunds(theUser, sc, WITHDRAW);
            case 3 -> ATM.updateFunds(theUser, sc, DEPOSIT);
            case 4 -> ATM.transferFunds(theUser,bankList, sc);
            case 5 -> ATM.changePassword(theUser, sc);
            case 6 -> ATM.changeAccountName(theUser, sc);
            case 7 -> sc.nextLine(); // gobble up rest of previous input
        }

        // redisplay this menu unless the user wants to quit
        if (choice != 7) {
            ATM.printUserMenu(bankList, theUser,theBank, sc);
        }

    }

    /**
     * Process transferring funds from one account to another.
     *
     * @param theUser the logged-in User object
     * @param sc      the Scanner object used for user input
     */

    public static void transferFunds(User theUser, ArrayList<Bank> banks, Scanner sc) {
        Account fromAcct;
        Account toAcct = null;
        int toAcctID;
        int[] accountInfo;
        double amount;
        double transferLimit;

        int choice;
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
            toAcctID = toAcct.getAccountID();
        }
        else{
            //get third party account to transfer to
            accountInfo = Util.getThirdPartyTransferAccount(banks, sc);
            toAcctID = accountInfo[0];
            int bankID = accountInfo[1];
            if (bankID != -1){ // if account exists locally
                toAcct = banks.get(bankID).getAccount(toAcctID);
            }
        }
        // get amount to transfer
        amount = Util.getTransferAmount(transferLimit, sc);

        // get memo for transfer
        System.out.println("Enter memo for this transaction: ");
        String memo = sc.nextLine();

        // add transaction and update balance of fromAcct
        fromAcct.addTransaction(-amount, toAcctID, memo);
        fromAcct.addBalance(-amount);

        // if toAcct exists locally, add transaction locally + on sql, and update balance locally,
        if (toAcct != null) {
            toAcct.addTransaction(amount, fromAcct.getAccountID(), memo);
            toAcct.addBalance(amount);
        }
        else{ // if toAcct doesnt exist locally, only add transaction only on sql
            Transaction newTrans = new Transaction(amount, toAcctID, fromAcct.getAccountID(), memo);
            DB_Util.addTransactionToSQL(newTrans);
        }

        //update balance on SQL for both accounts
        DB_Util.updateSQLBalance(-amount, fromAcct.getAccountID());
        DB_Util.updateSQLBalance(amount, toAcctID);
    }

    /**
     * Process a fund withdraw from an account.
     * @param theUser	the logged-in User object
     * @param sc		the Scanner object used for user input
     */
    public static void updateFunds(User theUser, Scanner sc, int direction) {

        Account fromAcct;
        double amount;
        double withdrawLimit = -1;
        String memo;
        String directionString;

        // get account to withdraw from
        if (direction == WITHDRAW) {
            directionString = "withdraw from";
        }
        else{
            directionString = "transfer to";
        }

        fromAcct = Util.getInternalTransferAccount(theUser, directionString, sc);
        if (direction == WITHDRAW){ // if making a withdraw, set a limit
            withdrawLimit = fromAcct.getBalance();
        }

        // get amount to transfer
        amount = Util.getTransferAmount(withdrawLimit, sc);
        // make amount negative if withdrawing (WITHDRAW is = -1 while DEPOSIT is = 1)
        amount = direction * amount;
        // gobble up rest of previous input
        sc.nextLine();

        // get a memo
        System.out.print("Enter a memo: ");
        memo = sc.nextLine();

        // do the withdrawal
        // receiverID is -1 when its an internal transaction (deposit/withdrawal)
        fromAcct.addTransaction(amount, -1, memo);
        fromAcct.addBalance(amount);
        // update balance on SQL
        DB_Util.updateSQLBalance(fromAcct.getBalance(),fromAcct.getAccountID());
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

        // Update password on SQL
        DB_Util.changePassword(pin,theUser.getUUID());
    }

    /**
     * Change user account name.
     * @param theUser	the logged-in User object
     * @param sc		the Scanner object used for user input
     */
    public static void changeAccountName(User theUser, Scanner sc) { // Account acc
        System.out.print("Enter the accountNo which you would like to change the name: ");
        int usrChoice = sc.nextInt();
        theUser.getAcctUUID(usrChoice);


        if (usrChoice == theUser.getAcctUUID(usrChoice)){
            System.out.println("Enter new account name: ");
            System.out.println(theUser.getAccounts());
            String newName = sc.nextLine();
            //acc.setName(newName);
        }




        System.out.println("Account name successfully changed. ");
    }
    //TODO: add unit testing function(s)

}