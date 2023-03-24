package com.CLI.ATM.ATM2.controller;

import com.CLI.ATM.ATM2.model.*;
import com.CLI.ATM.ATM2.service.*;
import static com.CLI.ATM.ATM2.Constants.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class accountController {

    @Autowired
    UserService userService;

    @Autowired
    BankService bankService;

    @Autowired
    SQLService sqlService;

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

        return "accounts.html";
    }

}
