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

    private int uuid;

    private String pinHash;

    private ArrayList<Account> accounts;


}


