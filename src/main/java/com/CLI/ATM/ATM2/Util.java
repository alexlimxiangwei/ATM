package com.CLI.ATM.ATM2;

import com.CLI.ATM.ATM2.CLI.MainCLI;
import com.CLI.ATM.ATM2.CLI.UserCLI;
import com.CLI.ATM.ATM2.model.Bank;
import com.CLI.ATM.ATM2.model.User;
import com.CLI.ATM.ATM2.service.BankService;
import com.CLI.ATM.ATM2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.MessageDigest;

import static com.CLI.ATM.ATM2.Constants.*;

// Java Class containing all event/error handlers to call in main file

@Component
public class Util {

    @Autowired
    UserService userService;

    @Autowired
    BankService bankService;

    @Autowired
    UserCLI userCLI;

    @Autowired
    MainCLI mainCLI;

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
    public static void handleQuitApp(){
        System.out.println("Thank you for using our ATM!");
        System.out.println("Quitting...");
        System.exit(0);
    }

    public void handleLogin(Bank currentBank) {
        User curUser = null;
        while (curUser == null) {
            int userID = readInt("Enter user ID:");
            if (userID == QUIT) {
                return;
            }
            String pin = readString("Enter pin: ");
            if (pin.equals(QUIT_STRING)) {
                return;
            }
            curUser = userService.userLogin(currentBank, userID, pin);
            if (curUser == null) {
                System.out.println("Invalid user ID or pin. Please try again.");
            }
        }
        mainCLI.handleUserMenu(curUser, currentBank);
    }

    //region Error Handlers

    /**
     * reads user input, will validate if input is an integer
     * @param prompt prompt to ask user for input
     * @return int input
     */
    public static int readInt(String prompt) {
        int input = 0;
        boolean valid = false;
        while (!valid) {
            try {
                System.out.print(prompt);
                if (sc.hasNextInt()) {
                    input = sc.nextInt();
                    if (input == QUIT){
                        return QUIT;
                    }
                    else {
                        valid = true;
                    }
                } else {
                    throw new Exception();
                }
                sc.nextLine();  // discard any other data entered on the line
            }
            catch(Exception e) {
                System.out.println("Please enter a valid integer.\n");
                sc.nextLine();  // discard any other data entered on the line
            }
        }
        return input;
    }
    /**
     * reads user input, will validate if input is an integer and between min and max
     * @param prompt prompt to ask user for input
     * @return int input
     */
    public static int readInt(String prompt, int min, int max) {
        int input = 0;
        boolean valid = false;
        while (!valid) {
            try {
                System.out.print(prompt);
                if (sc.hasNextInt()) {
                    input = sc.nextInt();
                    if (input == QUIT){
                        return QUIT;
                    }
                    else if (input >= min && input <= max) {
                        valid = true;
                    } else {
                        System.out.println("Number must be between " + min + " and " + max + ".\n");
                    }
                } else {
                    throw new Exception();
                }
                sc.nextLine();  // discard any other data entered on the line
            }
            catch(Exception e) {
                System.out.println("Please enter a valid integer.\n");
                sc.nextLine();  // discard any other data entered on the line
            }
        }
        return input;
    }
    /**
     * reads user input, will validate if input is a double and between min and max
     * @param prompt prompt to ask user for input
     * @return double input
     */
    public static double readDouble(String prompt, double min, double max){
        double input = 0;
        boolean valid = false;
        while (!valid) {
            try {
                System.out.print(prompt);
                if (sc.hasNextDouble()) {
                    input = sc.nextDouble();
                    if (input == QUIT){
                        return QUIT;
                    }
                    else if (input >= min && input <= max) {
                        valid = true;
                    } else {
                        System.out.println("Number must be between " + min + " and " + max + ".\n");
                    }
                } else {
                    throw new Exception();
                }
            }
            catch(Exception e) {
                System.out.println("Please enter a valid number.\n");
                sc.nextLine();  // discard any other data entered on the line
            }
        }
        return input;
    }
    /**
     * reads user input, will validate if input is a proper string
     * @param prompt prompt to ask user for input
     * @return string input
     */
    public static String readString(String prompt){
        String input = "";
        boolean valid = false;
        while (!valid) {
            try {
                System.out.print(prompt);
                if (sc.hasNextLine()) {
                    input = sc.nextLine();
                    if (input.equals(Integer.toString(QUIT))){
                        return "-1";
                    }
                    else {
                        valid = true;
                    }
                } else {
                    throw new Exception();
                }
            }
            catch(Exception e) {
                System.out.println("Please enter a valid string.\n");
                sc.nextLine();  // discard any other data entered on the line
            }
        }
        return input;
    }
}


