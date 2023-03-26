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
public class Account {

    private String name;
    private int accountID;
    private User user;
    private ArrayList<Transaction> transactions;
    private Double balance;


}