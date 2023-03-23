package com.CLI.ATM.ATM2;

import com.CLI.ATM.ATM2.CLI.MainCLI;
import com.CLI.ATM.ATM2.model.Bank;
import com.CLI.ATM.ATM2.model.User;
import com.CLI.ATM.ATM2.service.AccountService;
import com.CLI.ATM.ATM2.service.BankService;
import com.CLI.ATM.ATM2.service.SQLService;
import com.CLI.ATM.ATM2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static com.CLI.ATM.ATM2.Constants.*;

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
	SQLService SQLService;
	@Autowired
	MainCLI mainCli;

	@Override
	public void run(String... args){
		//region INIT
		SQLService.initSQLConnection();
		bankList = SQLService.fetchBanks(); // gets all banks from database and stores it in an array
		User curUser = null;

		//endregion

		// loop forever
		while (true) {
			//region SET BANK TO USE
			// displayBankSelectionPage
			mainCli.displayBankSelectionPage(bankList);

			// ask for user's bank choice
			int userInput = sc.nextInt();
			sc.nextLine();

			// sets currentBank to user's bank choice
			Bank currentBank = bankList.get(userInput - 1);
			//endregion

			//region MAIN MENU
			// displaySignupPage
			Strings.displaySignUpMenuPage(currentBank);

			// stay in login prompt until successful login
			userInput = sc.nextInt();
			sc.nextLine();
			//endregion

			//region HANDLE USER CHOICE
			if (userInput == 1){
				curUser = accountService.handleLogIn(currentBank);
				mainCli.printUserMenu(bankList, curUser, currentBank);
			} else if (userInput == 2) {
				accountService.handleSignUp(currentBank);
			} else {
				System.out.println("You have entered invalid number");
			}
			//endregion
		}
	}
}