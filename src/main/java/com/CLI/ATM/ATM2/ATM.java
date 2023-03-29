package com.CLI.ATM.ATM2;

import com.CLI.ATM.ATM2.CLI.MainCLI;
import com.CLI.ATM.ATM2.model.Bank;
import com.CLI.ATM.ATM2.model.User;
import com.CLI.ATM.ATM2.service.BankService;
import com.CLI.ATM.ATM2.service.AccountService;
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
	BankService bankService;
	@Autowired
	UserService userService;
	@Autowired
	AccountService accountService;
	@Autowired
	SQLService SQLService;
	@Autowired
	MainCLI mainCli;
	@Autowired
	Util util;

	@Override
	public void run(String... args) {
		//region INIT
		SQLService.initSQLConnection();
		bankList = SQLService.fetchBanks(); // gets all banks from database and stores it in an array
		User curUser = null;

		//endregion

		// loop forever
		while (true) {

			Strings.print_welcomeMessage();

			//region SET BANK TO USE

			// displayBankSelectionPage
			Strings.displayBankSelectionPage(bankList);
			int num_of_local_banks = bankService.getNumOfLocalBanks();

			// ask for user's bank choice
			int userInput = Util.readInt("Enter your choice of bank: ", 1, num_of_local_banks);

			//if user wants to quit
			if (userInput == QUIT) {
				Util.handleQuitApp();
			}

			// sets currentBank to user's bank choice
			Bank currentBank = bankList.get(userInput - 1);
			//endregion

			//region MAIN MENU
			// displaySignupPage
			while (userInput != QUIT) {
				Strings.displaySignUpMenuPage(currentBank);

				userInput = Util.readInt("Enter choice: ", 1, 2);

				//endregion

				//region HANDLE USER CHOICE
				switch (userInput) {
					case QUIT:
						System.out.println("Quitting from " + currentBank.getName() + "...\n");
						break;
					case 1:
						util.handleLogin(currentBank);
						break;
					case 2:
						accountService.handleSignUp(currentBank);
						break;
				}
				//endregion
			}
		}
	}
}