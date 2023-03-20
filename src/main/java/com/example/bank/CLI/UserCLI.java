package com.example.bank.CLI;


import com.example.bank.model.Account;
import com.example.bank.model.User;
import com.example.bank.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import static com.example.bank.CLI.CLITool.adjustSpacing;


@Component
public class UserCLI {

    @Autowired
    AccountService accountService;

    /**
     * prints a simplified accounts summary in the event user forgets what is their source and destination account
     */
    public void printAccountsSummarySimp(User user){
        System.out.printf("\n\n%s's accounts summary\n", user.getFirstName());
        for (int a = 0; a < user.getAccounts().size(); a++) {
            HashMap<String, String> summary = accountService.getSummaryLine(user.getAccounts().get(a));
            System.out.printf("%d) Name: %-18s |Balance: %-18s\n", a+1,
                    summary.get("name"), summary.get("balance"));
        }
    }

    /**
     * Print summaries for the accounts of this user.
     */

    public void printAccountsSummary(User user) {

        System.out.printf("\n\n%s %s's accounts summary\n", user.getFirstName(), user.getLastName());
        System.out.print(
                """
                        ╔════════════════════╦════════════════════╦════════════════════╗
                        ║ Name               ║ Account ID         ║ Balance            ║
                        """
        );

        for (Account account : user.getAccounts()) {
            System.out.println("╠════════════════════╬════════════════════╬════════════════════╣");
            HashMap<String, String> val = accountService.getSummaryLine(account);

            System.out.printf(
                    "║ %-18s║ %-18s║ %18s║\n",
                    adjustSpacing(val.get("name")),
                    adjustSpacing(val.get("uuid")),
                    adjustSpacing("$" + val.get("balance")));

        }
        System.out.println("╚════════════════════╩════════════════════╩════════════════════╝\n");
    }
}
