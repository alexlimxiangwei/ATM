package com.CLI.ATM.ATM2.controller;

import com.CLI.ATM.ATM2.model.*;
import com.CLI.ATM.ATM2.service.*;
import static com.CLI.ATM.ATM2.Constants.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.Comparator;
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

    @Autowired
    TransactionService transactionService;

    @GetMapping("/menuPage")
    public String getHomeHTML(Model model){
        Bank currBank = bankService.getBankFromID(bankList, HTML_currBankID);
        User currUser = bankService.getUserFromID(currBank, HTML_currUserID);
        String firstName = currUser.getFirstName();
        String lastName = currUser.getLastName();
        String userName = firstName + " " + lastName;

        model.addAttribute("fullName", userName);
        model.addAttribute("userId", HTML_currUserID);
        model.addAttribute("bankName", currBank.getName());

        return "menuPage";
    }

    @GetMapping("/accounts")
    public String getAccountsHTML(Model model){
        Bank currBank = bankService.getBankFromID(bankList, HTML_currBankID);
        User currUser = bankService.getUserFromID(currBank, HTML_currUserID);
        String firstName = currUser.getFirstName();
        String lastName = currUser.getLastName();
        String userName = firstName + " " + lastName;

        // show account summary on HTML
        List<Account> accountListing = currUser.getAccounts();
        model.addAttribute("fullName", userName);
        model.addAttribute("accounts", accountListing);

        return "accounts.html";
    }

//    @GetMapping("/transactions")
//    public String getTransactionsHTML(Model model){
//        getAccountsHTML(model);
//        return "transactions.html";
//    }

    @PostMapping("/accounts/deposit")
    public String depositHTML(Model mode,
                              @RequestParam("accId-deposit") int accId,
                              @RequestParam("deposit") double amount,
                              @RequestParam("memo-deposit") String memo) {

        Bank currBank = bankService.getBankFromID(bankList, HTML_currBankID);
        User currUser = bankService.getUserFromID(currBank, HTML_currUserID);
        Account currAcc = accountService.getAccountFromID(currUser, accId);

        accountService.addTransaction(currAcc, amount, TRANSACTION_TO_SELF, memo);
        accountService.addBalance(currAcc, amount);

        // update balance on SQL
        sqlService.updateBalance(currAcc.getBalance(),currAcc.getAccountID());
        return "redirect:/accounts";
    }

    @PostMapping("/accounts/withdraw")
    public String withdrawHTML(Model model,
                              @RequestParam("accId-withdraw") int accId,
                              @RequestParam("withdraw") double amount,
                              @RequestParam("memo-withdraw") String memo) {

        Bank currBank = bankService.getBankFromID(bankList, HTML_currBankID);
        User currUser = bankService.getUserFromID(currBank, HTML_currUserID);
        Account currAcc = accountService.getAccountFromID(currUser, accId);

        amount = -amount;

        accountService.addTransaction(currAcc, amount, TRANSACTION_TO_SELF, memo);
        accountService.addBalance(currAcc, amount);

        // update balance on SQL
        sqlService.updateBalance(currAcc.getBalance(),currAcc.getAccountID());

        return "redirect:/accounts";
    }

    @PostMapping("/accounts/transfer")
    public String internalTransfer(Model model,
                                   @RequestParam("accId-transfer-from") int accIdFrom,
                                   @RequestParam("accId-transfer-to-internal") int accIdTo_Internal,
                                   @RequestParam("accId-transfer-to-external") int accIdTo_External,
                                   @RequestParam("transfer") double amount,
                                   @RequestParam("memo-transfer") String memo,
                                   @RequestParam("type-transfer") int transferType) {

        // need to add transfer methods here !!!

        return "redirect:/accounts";
    }

    @GetMapping("/transactions")
    public String transactionsHTML(Model model){
        getAccountsHTML(model);
        return "transactions";
    }

    @PostMapping("/transactions/showTransactionsForm")
    public String showTransactionsHTMLForm(Model model,
                                       @RequestParam("acctId") int acctId){

        Bank currBank = bankService.getBankFromID(bankList, HTML_currBankID);
        User currUser = bankService.getUserFromID(currBank, HTML_currUserID);
        Account currAcc = accountService.getAccountFromID(currUser, acctId);

        HTML_currAccID = currAcc.getAccountID();

        return "redirect:/transactions/showTransaction";
    }

    @GetMapping("/transactions/showTransaction")
    public String showTransactionHTML(Model model){

        getAccountsHTML(model);

        Bank currBank = bankService.getBankFromID(bankList, HTML_currBankID);
        User currUser = bankService.getUserFromID(currBank, HTML_currUserID);
        Account currAcc = accountService.getAccountFromID(currUser, HTML_currAccID);

        List<Transaction> transactionListing = currAcc.getTransactions();

        // implement date sorting

        model.addAttribute("transactions", transactionListing);
        model.addAttribute("currAcctID", HTML_currAccID);

        return "transactions";
    }
}
