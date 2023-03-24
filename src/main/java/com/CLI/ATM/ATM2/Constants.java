package com.CLI.ATM.ATM2;

import com.CLI.ATM.ATM2.model.Bank;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Scanner;

public class Constants {
    public static final int DEPOSIT = 1;
    public static final int WITHDRAW = -1;

    public static final double DEFAULT_LOCAL_TRANSFER_LIMIT = 1000;
    public static final double DEFAULT_OVERSEAS_TRANSFER_LIMIT = 1000;
    public static final int NOT_FOUND = -1;
    public static final int TRANSACTION_TO_SELF = -1;
    public static Connection conn;
    public static final Scanner sc = new Scanner(System.in);
    public static ArrayList<Bank> bankList;
    public static int HTML_currUserID = -1;
    public static int HTML_currBankID = -1;
    public static int HTML_currAccID = -1;
}
