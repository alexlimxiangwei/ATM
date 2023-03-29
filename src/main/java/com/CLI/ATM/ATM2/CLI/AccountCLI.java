package com.CLI.ATM.ATM2.CLI;

import com.CLI.ATM.ATM2.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.CLI.ATM.ATM2.Constants.PAGE_SIZE;
import static com.CLI.ATM.ATM2.Constants.sc;

@Component
public class AccountCLI {

    @Autowired
    TransactionCLI transactionCLI;

    /**
     * Print transaction history for account
     */
    public void printTransHistory(Account account) {
        int page = 1;
        int max_page = (int) Math.ceil((account.getTransactions().size() / PAGE_SIZE));
        System.out.printf("\nTransaction history for account %s:\n\n", account.getAccountID());

        while (page != max_page){
            for (int t = (int) ((page - 1) * PAGE_SIZE); t < (int) (page * PAGE_SIZE); t++) {
                System.out.println(transactionCLI.getSummaryLine(account.getTransactions().get(t)));
            }
            System.out.println("\nPage " + page + " of " + max_page);
            System.out.println("Press enter to see more transactions or -1 to exit");
            if (sc.nextLine().equals("-1")){
                break;
            }
            System.out.println();
            page++;
        }
    }
}
