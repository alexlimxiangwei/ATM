package com.CLI.ATM.ATM2.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String firstName;

    private String lastName;

    private int customerID;

    private String pinHash;

    private ArrayList<Account> accounts;

    private double local_transfer_limit;

    private double overseas_transfer_limit;


}


