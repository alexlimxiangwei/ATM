package com.example.bank.CLI;

import com.example.bank.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountCLI {

    @Autowired
    TransactionCLI transactionCLI;

    /**
     * Print transaction history for account
     */
    //TODO : make this filo I/O to store all transactions
    public void printTransHistory(Account account) {

        System.out.printf("\nTransaction history for account %s\n", account.getAccountID());
        for (int t = account.getTransactions().size()-1; t >= 0; t--) {
            System.out.println(transactionCLI.getSummaryLine(account.getTransactions().get(t)));
        }
        System.out.println();

    }
}
