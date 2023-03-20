package com.example.bank.util;


import com.example.bank.CLI.UserCLI;
import com.example.bank.service.BankService;
import com.example.bank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.bank.util.Util;

import java.math.BigInteger;
import java.security.MessageDigest;

// Java Class containing all event/error handlers to call in main file

@Component
public class Util {

    @Autowired
    UserService userService;

    @Autowired
    BankService bankService;

    @Autowired
    UserCLI userCLI;



    /**
     * Gets an internal account of a user for transferring purposes
     * by asking for user input
     * @param theUser	the user to loop through his accounts
     * @param directionString direction of transfer, e.g. : transfer to / withdraw from
     * @return Account object for transferring of $
     */

    /**
     * Gets amount to transfer from user, with an upper transfer limit
     * @param *limit// the upper $ amount limit of transfer, or -1 for no limit
     * @return the amount the user inputted
     */



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


