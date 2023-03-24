package com.CLI.ATM.ATM2.controller;

import com.CLI.ATM.ATM2.model.*;
import com.CLI.ATM.ATM2.service.*;
import static com.CLI.ATM.ATM2.Constants.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class accountController {

    @Autowired
    UserService userService;

    @Autowired
    BankService bankService;

    @Autowired
    SQLService sqlService;

    @Autowired
    AccountService accountService;

    @GetMapping("/menuPage")
    public String getHomeHTML(Model model){
        Bank currBank = bankService.getBankFromID(bankList, HTML_currBankID);
        User currUser = bankService.getUserByID(currBank, HTML_currUserID);
        String firstName = currUser.getFirstName();
        String lastName = currUser.getLastName();
        String userName = firstName + " " + lastName;

        model.addAttribute("fullName", userName);
        model.addAttribute("userId", HTML_currBankID);
        model.addAttribute("bankName", currBank.getName());

        return "menuPage";
    }

    @GetMapping("/accounts")
    public String getAccountsHTML(Model model){
        Bank currBank = bankService.getBankFromID(bankList, HTML_currBankID);
        User currUser = bankService.getUserByID(currBank, HTML_currUserID);
        String firstName = currUser.getFirstName();
        String lastName = currUser.getLastName();
        String userName = firstName + " " + lastName;

        // show account summary on HTML
        List<Account> accountListing = currUser.getAccounts();
        model.addAttribute("fullName", userName);
        model.addAttribute("accounts", accountListing);

        return "accounts.html";
    }

    @PostMapping("/accounts/deposit")
    public String depositHTML(Model mode,
                              @RequestParam("accId-deposit") int accId,
                              @RequestParam("deposit") double amount,
                              @RequestParam("memo-deposit") String memo) {

        Bank currBank = bankService.getBankFromID(bankList, HTML_currBankID);
        User currUser = bankService.getUserByID(currBank, HTML_currUserID);
        Account currAcc = currUser.getAccounts().get(accId);

        accountService.addTransaction(currAcc, amount, TRANSACTION_TO_SELF, memo);

        accountService.addBalance(currAcc, amount);

        // update balance on SQL
        sqlService.updateBalance(currAcc.getBalance(),currAcc.getAccountID());

        return "redirect:/accounts";
    }

    @PostMapping("/accounts/withdraw")
    public String withdrawHTML(Model mode,
                              @RequestParam("accId-withdraw") int accId,
                              @RequestParam("withdraw") double amount,
                              @RequestParam("memo-withdraw") String memo) {

        Bank currBank = bankService.getBankFromID(bankList, HTML_currBankID);
        User currUser = bankService.getUserByID(currBank, HTML_currUserID);
        Account currAcc = currUser.getAccounts().get(accId);
        amount = -amount;

        accountService.addTransaction(currAcc, amount, TRANSACTION_TO_SELF, memo);

        accountService.addBalance(currAcc, amount);

        // update balance on SQL
        sqlService.updateBalance(currAcc.getBalance(),currAcc.getAccountID());

        return "redirect:/accounts";
    }


}
