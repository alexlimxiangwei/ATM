package com.CLI.ATM.ATM2.CLI;

import com.CLI.ATM.ATM2.model.Transaction;
import com.CLI.ATM.ATM2.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Date;

@Component
public class TransactionCLI {


    public String getSummaryLine(Transaction transaction) {

        double amount = transaction.getAmount();
        String memo = transaction.getMemo();
        Date timestamp = transaction.getTimestamp();

        if (amount >= 0) {
            return String.format("%s, $%.02f : %s",
                    timestamp.toString(), amount, memo);
        } else {
            return String.format("%s, $(%.02f) : %s",
                    timestamp.toString(), -amount, memo);
        }
    }
}
