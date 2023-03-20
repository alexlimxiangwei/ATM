package com.example.bank.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.ArrayList;


@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Bank {

    private int bankID;

    private String name;

    private boolean local;

    private ArrayList<User> users;


    private ArrayList<Account> accounts;


}