package com.CLI.ATM.ATM2.controller;

import com.CLI.ATM.ATM2.Util;
import com.CLI.ATM.ATM2.model.*;
import com.CLI.ATM.ATM2.service.*;
import static com.CLI.ATM.ATM2.Constants.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


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
        String firstName = HTML_currUser.getFirstName();
        String lastName = HTML_currUser.getLastName();
        String userName = firstName + " " + lastName;

        model.addAttribute("fullName", userName);
        model.addAttribute("userId", HTML_currUser.getCustomerID());
        model.addAttribute("bankName", HTML_currBank.getName());

        return "menuPage";
    }

    @GetMapping("/accounts")
    public String getAccountsHTML(Model model){
        String firstName = HTML_currUser.getFirstName();
        String lastName = HTML_currUser.getLastName();
        String userName = firstName + " " + lastName;

        // show account summary on HTML
        List<Account> accountListing = HTML_currUser.getAccounts();
        model.addAttribute("fullName", userName);
        model.addAttribute("accounts", accountListing);

        // error message box
        if (HTML_error){
            String message = "TRANSFER FAILED\nNo such Account ID [" + HTML_accIDNoExists +"] exists !";
            model.addAttribute("message", message);
        }

        return "accounts.html";
    }

    @PostMapping("/accounts/deposit")
    public String depositHTML(Model mode,
                              @RequestParam("accId-deposit") int accId,
                              @RequestParam("deposit") double amount,
                              @RequestParam("memo-deposit") String memo) {
        Account currAcc = accountService.getAccountFromID(HTML_currUser, accId);

        accountService.addTransaction(currAcc, amount, TRANSACTION_TO_SELF, memo, LOCAL_TRANSACTION);
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


        Account currAcc = accountService.getAccountFromID(HTML_currUser, accId);

        amount = -amount;

        accountService.addTransaction(currAcc, amount, TRANSACTION_TO_SELF, memo, LOCAL_TRANSACTION);
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

        User fromUser = HTML_currUser;
        Account fromAcct = accountService.getAccountFromID(fromUser, accIdFrom);
        Account toAcct = null;

        // return form if accountID doesn't exist
        if (accountService.getAccountFromID(fromUser, accIdTo_External) == null){
            HTML_accIDNoExists = accIdTo_External;
            HTML_error = true;
        }else {

            switch (transferType) {
                case 1 -> { // internal transfer
                    toAcct = accountService.getAccountFromID(fromUser, accIdTo_Internal);

                    // add transaction locally
                    accountService.addTransaction(fromAcct, -amount, TRANSACTION_TO_SELF, memo, LOCAL_TRANSACTION);
                    accountService.addTransaction(toAcct, amount, TRANSACTION_TO_SELF, memo, LOCAL_TRANSACTION);
                }
                case 2 -> { // external transfer
                    int toBankID = accountService.validateThirdPartyAccount(accIdTo_External);
                    Bank toBank = bankService.getBankFromID(bankList, toBankID);
                    toAcct = bankService.getAccountFromID(toBank, accIdTo_External);
                    boolean isLocal = toBank.isLocal();
                    // add transaction locally
                    accountService.addTransaction(fromAcct, -amount, accIdTo_External, memo, isLocal);
                    accountService.addTransaction(toAcct, amount, accIdFrom, memo, isLocal);


                }
                default -> {
                }
            }
            accountService.addBalance(fromAcct, -amount);
            accountService.addBalance(toAcct, amount);
            //update on SQL
            sqlService.updateBalance(fromAcct.getBalance(), accIdFrom);
            sqlService.updateBalance(toAcct.getBalance(), accIdTo_External);

        }
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

        Account currAcc = accountService.getAccountFromID(HTML_currUser, acctId);
        HTML_currAccID = currAcc.getAccountID();

        return "redirect:/transactions/showTransaction";
    }

    @GetMapping("/transactions/showTransaction")
    public String showTransactionHTML(Model model){

        getAccountsHTML(model);

        Account currAcc = accountService.getAccountFromID(HTML_currUser, HTML_currAccID);
        List<Transaction> transactionListing = currAcc.getTransactions();

        // implement date sorting

        model.addAttribute("transactions", transactionListing);
        model.addAttribute("currAcctID", HTML_currAccID);

        return "transactions";
    }

    @GetMapping("/settings")
    public String getSettingsHTML(Model model){

        getAccountsHTML(model);

        String firstName = HTML_currUser.getFirstName();
        String lastName = HTML_currUser.getLastName();

        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("userId", HTML_currUserID);
        model.addAttribute("bankName", HTML_currBank.getName());

        return "settings";
    }

    @PostMapping("/settings/changePassword")
    public String changePasswordHTML(Model model,
                                     @RequestParam("current_pin") String current_pin,
                                     @RequestParam("new_pin") String new_pin,
                                     @RequestParam("confirm_pin") String confirm_pin){
        if (!new_pin.equals(confirm_pin)){
            System.out.println("new pin does not match confirm pin");// TODO: maybe show error message
        }
        else if (!HTML_currUser.getPinHash().equals(Util.hash(current_pin))){
            System.out.println("current pin does not match entered pin!");// TODO: maybe show error message
        }
        else{
            HTML_currUser.setPinHash(Util.hash(new_pin));
            sqlService.changePassword(Util.hash(new_pin), HTML_currUserID , HTML_currBank);
        }
        return "redirect:/settings";
    }

    @PostMapping("/settings/createAcc")
    public String createNewAccHTML(Model model,
                                   @RequestParam("newAccName") String newAccName){

        // add create account functions
        Account newAccount = accountService.createAccount(newAccName, HTML_currUser, 0.00);
        HTML_currUser.getAccounts().add(newAccount);

        // Update add account on sql
        sqlService.addAccount(newAccount.getAccountID(), HTML_currUser.getCustomerID(), HTML_currBank.getBankID(), newAccName, 0.00);
        return "redirect:/settings";
    }

    @PostMapping("/settings/editAcc")
    public String editAccHTML(Model model,
                              @RequestParam("editedAccName") String editedAccName,
                              @RequestParam("accID_edit") int accID){

        getAccountsHTML(model);
        // add edit account functions
        accountService.getAccountFromID(HTML_currUser, accID).setName(editedAccName);
        sqlService.changeAccountName(accID, editedAccName);

        return "redirect:/settings";
    }

    @PostMapping("/settings/deleteAcc")
    public String deleteAccHTML(Model model,
                                @RequestParam("accID_delete") int accID){

        getAccountsHTML(model);
        // add delete account functions
        userService.deleteAccount(HTML_currUser, accID);

        // Update deleted account on sql
        sqlService.deleteAccount(accID);
        return "redirect:/settings";
    }
}
