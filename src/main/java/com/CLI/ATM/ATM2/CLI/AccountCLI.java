package com.CLI.ATM.ATM2.CLI;

import com.CLI.ATM.ATM2.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountCLI {

    @Autowired
    TransactionCLI transactionCLI;

    /**
     * Print transaction history for account
     */
    public void printTransHistory(Account account) {

        System.out.printf("\nTransaction history for account %s:\n\n", account.getAccountID());
        for (int t = 0; t < account.getTransactions().size(); t++) {
            System.out.println(transactionCLI.getSummaryLine(account.getTransactions().get(t)));
        }
        System.out.println();

    }
}
