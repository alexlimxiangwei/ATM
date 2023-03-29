package com.CLI.ATM.ATM2.controller;

import com.CLI.ATM.ATM2.Util;
import com.CLI.ATM.ATM2.model.*;
import com.CLI.ATM.ATM2.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import static com.CLI.ATM.ATM2.Constants.*;

@Controller
public class userController {

    @Autowired
    UserService userService;

    @Autowired
    BankService bankService;

    @Autowired
    AccountService accountService;

    @Autowired
    SQLService sqlService;

    //signIn page
    @GetMapping("/")
    public String home(Model model) {
        return "redirect:/signInPage";
    }

    @RequestMapping(value = "/signInPage", method = RequestMethod.GET)
    public String populateList(Model model) {
        List<Bank> bankListing = sqlService.fetchBanks();
        for (int i = 0; i < bankListing.size(); i++){
            if (!bankListing.get(i).isLocal()){
                bankListing.remove(i);
            }
        }
        model.addAttribute("bankListing", bankListing);
        return "signInPage.html";
    }

    @RequestMapping(value = "/signUpPage", method = RequestMethod.GET)
    public String populateList2(Model model) {
        List<Bank> bankListing = sqlService.fetchBanks();
        for (int i = 0; i < bankListing.size(); i++){
            if (!bankListing.get(i).isLocal()){
                bankListing.remove(i);
            }
        }
        model.addAttribute("bankListing", bankListing);
        return "signUpPage.html";
    }

    @PostMapping("/signInPage")
    public String getSignInDetails(@RequestParam("userid") int userid,
                                   @RequestParam("pin") String pin,
                                   @RequestParam("bankDropdown") int bankid,
                                   Model model, User user){

        populateList(model);
        Bank bankObj = bankService.getBankFromID(bankList, bankid);

        assert bankObj != null;
        User authUser = userService.userLogin(bankObj, userid, pin);

        // print html authentication for userLogin
        String message = "No message here";
        if (authUser == null){
            message = "Wrong Login Credentials";
            model.addAttribute("message", message);
        }else{
            HTML_currBank = bankService.getBankFromID(bankList, bankid);
            HTML_currUser = bankService.getUserFromID(HTML_currBank, userid);
            HTML_currUserID = userid;
            return "redirect:/menuPage";
        }
        return "signInPage";
    }

    @PostMapping("/signUpPage")
    public String submitUser(@RequestParam("firstName") String firstName,
                             @RequestParam("lastName") String lastName,
                             @RequestParam("pin") String pin,
                             @RequestParam("bankDropdown") int bankid,
                             Model model) {

        populateList2(model);
        System.out.println(firstName);
        System.out.println(lastName);
        System.out.println(pin);
        System.out.println(bankid);

        User newuser = new User();
        newuser.setFirstName(firstName);
        newuser.setLastName(lastName);
        String newPin = Util.hash(pin);
        Bank currentBank = bankList.get(bankid);

        // Creates a new user account based on user input
        User newUser2 = bankService.addUserToBank(currentBank, firstName, lastName, newPin,
                DEFAULT_LOCAL_TRANSFER_LIMIT, DEFAULT_OVERSEAS_TRANSFER_LIMIT);
        Account newAccount2 = accountService.createAccount("CHECKING", newUser2, 0.0);
        userService.addAccountToUser(newUser2, newAccount2);
        bankService.addAccountToBank(currentBank, newAccount2);

        System.out.println("Account successfully created.");

        // Add new user to SQL
        sqlService.addNewUser(newUser2.getCustomerID(),firstName,lastName,newPin,currentBank,
                DEFAULT_LOCAL_TRANSFER_LIMIT, DEFAULT_OVERSEAS_TRANSFER_LIMIT);
        sqlService.addAccount(newAccount2.getAccountID(),newUser2.getCustomerID(),currentBank.getBankID(),"Savings",0.00);

        System.out.println(newUser2.getCustomerID());
        System.out.println(currentBank.getBankID());

        return "redirect:/signInPage";
    }
}
