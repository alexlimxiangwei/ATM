package com.CLI.ATM.ATM2;

import com.CLI.ATM.ATM2.CLI.UserCLI;
import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.Bank;
import com.CLI.ATM.ATM2.model.User;
import com.CLI.ATM.ATM2.service.BankService;
import com.CLI.ATM.ATM2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Scanner;

// Java Class containing all event/error handlers to call in main file

@Component
public class Util {

    @Autowired
    UserService userService;

    @Autowired
    BankService bankService;

    @Autowired
    UserCLI userCLI;

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
}


