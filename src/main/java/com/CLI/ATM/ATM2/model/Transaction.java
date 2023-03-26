package com.CLI.ATM.ATM2.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {


    private double amount;

    private Date timestamp;

    private String memo;

    private int accountID;

    private int receiverID;

    private int transactionID;




}