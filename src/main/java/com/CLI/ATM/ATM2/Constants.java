package com.CLI.ATM.ATM2;

import com.CLI.ATM.ATM2.model.Bank;
import com.CLI.ATM.ATM2.model.User;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Scanner;

public class Constants {
    public static final int DEPOSIT = 1;
    public static final int WITHDRAW = -1;
    public static final int QUIT = -1;
    public static final String QUIT_STRING = "-1";
    public static final double PAGE_SIZE = 10;
    public static final boolean LOCAL_TRANSACTION = true;
    public static final double DEFAULT_LOCAL_TRANSFER_LIMIT = 1000;
    public static final double DEFAULT_OVERSEAS_TRANSFER_LIMIT = 1000;
    public static final int NOT_FOUND = -1;
    public static final int TRANSACTION_TO_SELF = -1;
    public static Connection conn;
    public static final Scanner sc = new Scanner(System.in);
    public static ArrayList<Bank> bankList;
    public static int HTML_currUserID = -1;
    public static int HTML_currAccID = -1;
    public static User HTML_currUser;
    public static Bank HTML_currBank;

    public static boolean HTML_transferError;
    public static boolean HTML_withdrawError;
    public static int HTML_accIDExists;
    public static int HTML_newUserID = -1;

}
