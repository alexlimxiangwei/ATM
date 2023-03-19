package com.CLI.ATM.ATM2;

import com.CLI.ATM.ATM2.CLI.MainCLI;
import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.Bank;
import com.CLI.ATM.ATM2.model.User;
import com.CLI.ATM.ATM2.service.AccountService;
import com.CLI.ATM.ATM2.service.BankService;
import com.CLI.ATM.ATM2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import static com.CLI.ATM.ATM2.Constants.conn;

@SpringBootApplication
public class ATM implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ATM.class, args);
	}

	@Autowired
	UserService userService;

	@Autowired
	BankService bankService;

	@Autowired
	AccountService accountService;

	@Autowired
	MainCLI mainCli;


	@Override
	public void run(String... args){
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
		ArrayList<Bank> bankList = bankService.fetchBanks(); // gets all banks from database and stores it in an array

		// init Scanner
		Scanner sc = new Scanner(System.in);

		User curUser;

		// continue looping forever
		while (true) {

			// displayBankSelectionPage
			mainCli.displayBankSelectionPage(bankList);

			int userInput = sc.nextInt();
			sc.nextLine();
			Bank currentBank = bankList.get(userInput - 1);

			// displaySignupPage
			mainCli.displaySignUpMenuPage(currentBank);

			// stay in login prompt until successful login
			userInput = sc.nextInt();
			sc.nextLine();

			if (userInput == 1){
				// stay in login prompt until successful login
				curUser = mainCli.mainMenuPrompt(currentBank, sc);
				System.out.printf("Login Successful.\nWelcome, %s!", curUser.getFirstName());
				// stay in main menu until user quits
				mainCli.printUserMenu(bankList, curUser, currentBank, sc);

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
				String newPin = Util.hash(sc.nextLine()); //hash it immediately, so we don't store the password at all

//              Creates a new user account based on user input
				User newUser2 = bankService.addUserToBank(currentBank, fname, lname, newPin);
				Account newAccount2 = accountService.createAccount("CHECKING", newUser2, 0.0);
				userService.addAccountToUser(newUser2, newAccount2);
				bankService.addAccountToBank(currentBank, newAccount2);

				System.out.println("Account successfully created.");

				// Add new user to SQL
				userService.addNewUser(newUser2.getUuid(),fname,lname,newPin,currentBank);
				accountService.SQL_addAccount(newAccount2.getAccountID(),newUser2.getUuid(),currentBank.getBankID(),"Savings",0.00);

			} else {
				System.out.println("You have entered invalid number");
			}
		}
	}
}