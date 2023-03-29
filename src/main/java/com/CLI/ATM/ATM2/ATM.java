package com.CLI.ATM.ATM2;

import com.CLI.ATM.ATM2.model.Bank;
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

	//region FIELDS
	@Autowired
	BankService bankService;
	@Autowired
	UserService userService;
	@Autowired
	SQLService SQLService;
	@Autowired
	Util util;
	//endregion

	@Override
	public void run(String... args) {
		//region INIT
		SQLService.initSQLConnection();
		bankList = SQLService.fetchBanks(); // gets all banks from database and stores it in an array

		//endregion

		// loop forever
		while (true) {

			Strings.print_welcomeMessage();

			//region SET BANK TO USE

			// displayBankSelectionPage
			Strings.print_BankSelectionPage(bankList);
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

			// displaySignupPage
			while (userInput != QUIT) {
				Strings.print_SignUpMenuPage(currentBank);

				userInput = Util.readInt("Enter choice: ", 1, 2);

				//region HANDLE USER CHOICE
				switch (userInput) {
					case QUIT -> System.out.println("Quitting from " + currentBank.getName() + "...\n");
					case 1 -> util.handleLogin(currentBank);
					case 2 -> userService.handleSignUp(currentBank);
				}
				//endregion
			}
		}
	}
}