
import java.util.ArrayList;
import java.util.HashMap;


public class User extends CLITools{


    /**
     * The first name of the user.
     */
    private String firstName;

    /**
     * The last name of the user.
     */
    private String lastName;

    /**
     * The ID number of the user.
     */
    private int uuid;

    /**
     * The hash of the user's.
     */
    private String pinHash;

    /**
     * The list of accounts for this user.
     */
    private ArrayList<Account> accounts;


    /**
     * Default User class
     */
    public User(){

    }

    /**
     * Create new user
     * @param firstName	the user's first name
     * @param lastName	the user's last name
     * @param pin				the user's account pin (as a String)
     * @param theBank		the bank that the User is a customer of
     */
    public User (String firstName, String lastName, String pin, Bank theBank) {

        // set user's name
        this.firstName = firstName;
        this.lastName = lastName;

        // store the pin's SHA-256 hash, rather than the original value, for
        // security reasons
        this.pinHash = pin;
        // get a new, unique universal unique ID for the user
        this.uuid = theBank.getNewUserUUID();

        // create empty list of accounts
        this.accounts = new ArrayList<>();

        // print log message
        System.out.printf("New user %s, %s with ID %s created.\n",
                lastName, firstName, this.uuid);

    }

    public User (int uuid, String firstName, String lastName, String pin, Bank theBank) {
        this.uuid = uuid;
        // set user's name
        this.firstName = firstName;
        this.lastName = lastName;

        // store the pin's MD5 hash, rather than the original value, for
        // security reasons
        this.pinHash = pin;

        // create empty list of accounts
        this.accounts = new ArrayList<>();

        // print log message
        System.out.printf("New user %s, %s with ID %s created.\n",
                lastName, firstName, this.uuid);

    }

    /**
     * Get the user ID number
     * @return	the uuid
     */
    public int getUUID() {
        return this.uuid;
    }

    /**
     * Add an account for the user.
     * @param anAcct	the account to add
     */

    // TODO: getters and setters for name field, setter for pin (so user can change passwords), reset password function
    public void addAccount(Account anAcct) {
        this.accounts.add(anAcct);
    }

    /**
     * Get the number of accounts the user has.
     * @return	the number of accounts
     */
    public int numAccounts() {
        return this.accounts.size();
    }

    /**
     * Get the balance of a particular account.
     * @param acctIdx	the index of the account to use
     * @return			the balance of the account
     */
    public double getAcctBalance(int acctIdx) {
        return this.accounts.get(acctIdx).getBalance();
    }

    /**
     * Get the UUID of a particular account.
     *
     * @param acctIdx the index of the account to use
     * @return the UUID of the account
     */
    public int getAcctUUID(int acctIdx) {
        return this.accounts.get(acctIdx).getAccountID();
    }
    /**
     * Get a particular account.
     * @param acctIndex	the index of the account to use
     * @return			The account object
     */
    public Account getAcct(int acctIndex) {
        return this.accounts.get(acctIndex);
    }
    /**
     * Print transaction history for a particular account.
     * @param acctIdx	the index of the account to use
     */
    public void printAcctTransHistory(int acctIdx) {
        this.accounts.get(acctIdx).printTransHistory();
    }



    /**
     * Check whether a given pin matches the true User pin
     * @param aPin	the pin to check
     * @return		whether the pin is valid or not
     */
    public boolean validatePin(String aPin) {
        return Util.hash(aPin).equals(this.pinHash);
    }

    // set pin
    public void setPin(String newPin) {
        this.pinHash = Util.hash(newPin);
    }

    public String getPin(){
        return pinHash;
    }

    // Get first name for new acc
    public String getFirstName() {
        return firstName;
    }

    // Set first name for new acc
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * prints a simplified accounts summary in the event user forgets what is their source and destination account
     */
    public void printAccountsSummarySimp(){
        System.out.printf("\n\n%s's accounts summary\n", this.firstName);
        for (int a = 0; a < this.accounts.size(); a++) {
            HashMap<String, String> summary = this.accounts.get(a).getSummaryLine();
            System.out.printf("%d) Name: %-18s |Balance: %-18s\n", a+1,
                    summary.get("name"), summary.get("balance"));
        }
    }


    /**
     * Print summaries for the accounts of this user.
     */

    public void printAccountsSummary() {

        System.out.printf("\n\n%s %s's accounts summary\n", this.firstName, this.lastName);
        System.out.print(
                """
                        ╔════════════════════╦════════════════════╦════════════════════╗
                        ║ Name               ║ Account ID         ║ Balance            ║
                        """
        );

        for (Account account : this.accounts) {
            System.out.println("╠════════════════════╬════════════════════╬════════════════════╣");
            HashMap<String, String> val = account.getSummaryLine();

            System.out.printf(
                    "║ %-18s║ %-18s║ %18s║\n",
                    adjustSpacing(val.get("name")),
                    adjustSpacing(val.get("uuid")),
                    adjustSpacing("$" + val.get("balance")));

        }
        System.out.println("╚════════════════════╩════════════════════╩════════════════════╝\n");
    }

    public ArrayList<Account> getAccounts() {
        return accounts;
    }



}


