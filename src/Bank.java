
import java.util.ArrayList;
import java.util.Random;

public class Bank {
    /**
     * The id of the bank.
     */
    private int bankID;

    /**
     * The name of the bank.
     */
    private String name;

    /**
     * Whether the bank is local or overseas
     */
    private boolean local;

    /**
     * The account holders of the bank.
     */
    private ArrayList<User> users;

    /**
     * The accounts of the bank.
     */
    private ArrayList<Account> accounts;

    /**
     * Create a new Bank object with empty lists of users and accounts.
     */
    public Bank(int bankID, String name, boolean local) {
        this.bankID = bankID;
        this.name = name;
        this.local = local;
        // init users and accounts
        users = new ArrayList<User>();
        accounts = new ArrayList<Account>();

    }

    /**
     * Generate a new universally unique ID for a user.
     * @return	the uuid
     */
    public int getNewUserUUID() {

        // inits
        String uuid;
        Random rng = new Random();
        int len = 6;
        boolean nonUnique;

        // continue looping until we get a unique ID
        do {

            // generate the number
            uuid = "";
            for (int c = 0; c < len; c++) {
                uuid += ((Integer)rng.nextInt(10)).toString();
            }

            // check to make sure it's unique
            nonUnique = false;
            for (User u : this.users) {
                if (uuid.compareTo(String.valueOf(u.getUUID())) == 0) {
                    nonUnique = true;
                    break;
                }
            }

        } while (nonUnique);

        return Integer.parseInt(uuid);
    }

    /**
     * Generate a new universally unique ID for an account.
     * @return	the uuid
     */
    public int getNewAccountID() { //TODO: maybe static this or move to util

        // inits
        int id;
        Random rng = new Random();
        int len = 10;
        boolean nonUnique = false;

        // continue looping until we get a unique ID
        do {

            // generate the number
            id = 0;
            for (int c = 0; c < len; c++) {
                id += ((Integer)rng.nextInt(10));
            }

            // check to make sure it's unique
            for (Account a : this.accounts) {
                if (id == a.getAccountID()) {
                    nonUnique = true;
                    break;
                }
            }

        } while (nonUnique);

        return id;

    }

    /**
     * Create a new user of the bank.
     * @param firstName	the user's first name
     * @param lastName	the user's last name
     * @param pin				the user's pin
     * @return					the new User object
     */
    public User addUser(String firstName, String lastName, String pin) {

        // create a new User object and add it to our list
        User newUser = new User(firstName, lastName, pin, this);
        this.users.add(newUser);

        // create a savings account for the user and add it to our list
        Account newAccount = new Account("Savings", newUser, this, 0.0);
        newUser.addAccount(newAccount);
        this.accounts.add(newAccount);

        return newUser;

    }
    /**
     * Adds an existing user (that exists in sql but not in local mem) and his accounts to bank.
     * @param idCustomer Users id
     * @param firstName first name of user to add to the bank
     * @param lastName last name of user to add to the bank
     * @param pin hashed pin of the user
     * @return the created User object
     */
    public User addExistingUser(int idCustomer, String firstName, String lastName, String pin) {

        User existingUser = new User(idCustomer, firstName, lastName, pin, this);

        // adds user to banks list of users
        this.users.add(existingUser);

        //fetches all accounts and transactions belonging to user from sql
        DB_Util.addAccountsToUser(existingUser, this);
        // adds all users accounts to banks list of accounts
        this.accounts.addAll(existingUser.getAccounts());
        return existingUser;

    }

    /**
     * Add an existing account for a particular User.
     * @param newAccount	the account
     */
    public void addAccount(Account newAccount) {
        this.accounts.add(newAccount);
    }

    /**
     * Get the User object associated with a particular userID and pin, if they
     * are valid.
     * @param userID	the user UUID to log in
     * @param pin		the associate pin of the user
     * @return			the User object, if login is successfull, or null, if
     * 					it is not
     */
    public User userLogin(int userID, String pin) {

        // search through list of users
        for (User u : this.users) {

            // if we find the user, and the pin is correct, return User object
            if (u.getUUID() == userID)
            {
                if (u.validatePin(pin)){
                    return u;
                }
                else {
                    return null;
                }
            }
        }
        //If userId isnt found locally, search sql database
        System.out.println("User not found, attempting to fetch user from database...");
        User u = DB_Util.addExistingUser(this, userID);
        if (u != null && u.getUUID() == userID && u.getPin().equalsIgnoreCase(Util.hash(pin))) {
            return u;
        }
        // if we haven't found the user or have an incorrect pin, return null
        return null;

    }

    /**
     * Get the name of the bank.
     * @return	the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets an Account's index in accounts by searching accountID
     *
     * @return index of found account or -1 if not found
     */
    public int getAccountIndex(int accountID){
        for (int i = 0 ; i < this.accounts.size(); i++){
            if (accounts.get(i).getAccountID() == accountID){
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets an Account using its index in this.accounts (only call this after getAccountIndex)
     *
     * @return Account object
     */
    public Account getAccount(int index){
        return accounts.get(index);
    }

    public int getBankID() {
        return bankID;
    }
}