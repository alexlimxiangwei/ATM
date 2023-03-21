package com.CLI.ATM.ATM2;

import static com.CLI.ATM.ATM2.Constants.sc;

public class Strings {

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
        System.out.println("  3) Change Account Type");
        System.out.println("  4) Delete Account");
        System.out.println("  5) Back");
        System.out.println();
        System.out.print("Enter choice: ");
    }
}
