package com.CLI.ATM.ATM2;

import com.CLI.ATM.ATM2.model.Bank;

import java.util.ArrayList;

import static com.CLI.ATM.ATM2.Constants.*;


public class Strings {

    public static void print_transfer_fund_choices(){
        System.out.println("Enter a choice below: ");
        System.out.println("1) Inter-account transfer");
        System.out.println("2) Third party transfer");
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
    public static void displayUserMenu(){

        System.out.println("What would you like to do?");
        System.out.println("  1) Show account transaction history");
        System.out.println("  2) Withdraw");
        System.out.println("  3) Deposit");
        System.out.println("  4) Transfer");
        System.out.println("  5) Account Setting");
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
    public static String askAmountPrompt(double limit) {
        if (limit == Double.POSITIVE_INFINITY){
            return "Enter the amount to transfer: $";
        } else {
            return String.format("Enter the amount to transfer (max $%.02f): $", limit);
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
