package com.CLI.ATM.ATM2;

import com.CLI.ATM.ATM2.model.Bank;

import java.util.ArrayList;

import static com.CLI.ATM.ATM2.Constants.*;


public class Strings {

    public static int transferFundsMenu(){
        int choice;
        do {
            System.out.println("Enter a choice below: ");
            System.out.println("1) Inter-account transfer");
            System.out.println("2) Third party transfer");
            choice = sc.nextInt();

        } while(choice < 0 || choice > 2);
        return choice;
    }

    public static void displayBankSelectionPage(ArrayList<Bank> bankList) {
        System.out.println("List of local banks:");
        int bank_no = 1;
        for (Bank bank : bankList) {
            if (bank.isLocal()) {
                System.out.printf("  %d) %s\n", bank_no, bank.getName());
                bank_no++;
            }
        }
    }
    public static int displayUserMenu(){
        int choice;
        do {
            System.out.println("What would you like to do?");
            System.out.println("  1) Show account transaction history");
            System.out.println("  2) Withdraw");
            System.out.println("  3) Deposit");
            System.out.println("  4) Transfer");
            System.out.println("  5) Account Setting"); // TODO: change transfer limits
            System.out.println("  6) Quit");
            System.out.println();
            System.out.print("Enter choice: ");
            choice = sc.nextInt();

            if (choice < 1 || choice > 6) {
                System.out.println("Invalid choice. Please choose 1-6.");
            }
        } while (choice < 1 || choice > 6);

        return choice;
    }

    public static void displaySignUpMenuPage(Bank currentBank) {
        System.out.printf("\nWelcome to %s !\n", currentBank.getName());
        System.out.println("What would you like to do?");
        System.out.println("  1) Log In");
        System.out.println("  2) Sign Up");
    }

    public static void print_welcomeMessage(){
        System.out.println("\n\nWelcome to the ATM!");
        System.out.println("Enter -1 to quit at any time!\n");
    }
    public static void print_AskAmount(double limit) {
        if (limit == -1){
            System.out.print("Enter the amount to transfer: $");
        } else {
            System.out.printf("Enter the amount to transfer (max $%.02f): $", limit);
        }
    }
    public static void print_AccountSettings(){
        System.out.println("Account Setting");
        System.out.println("  1) Change Password");
        System.out.println("  2) Create Account");
        System.out.println("  3) Change Account Name");
        System.out.println("  4) Delete Account");
        System.out.println("  5) Back");
        System.out.println();
        System.out.print("Enter choice: ");
    }
}
