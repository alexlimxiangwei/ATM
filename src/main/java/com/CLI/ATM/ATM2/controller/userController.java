package com.CLI.ATM.ATM2.controller;
import com.CLI.ATM.ATM2.CLI.AccountCLI;
import com.CLI.ATM.ATM2.CLI.MainCLI;
import com.CLI.ATM.ATM2.model.*;
import com.CLI.ATM.ATM2.Constants;
import com.CLI.ATM.ATM2.service.BankService;
import com.CLI.ATM.ATM2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.sound.midi.SysexMessage;

import static com.CLI.ATM.ATM2.Constants.bankList;
import static com.CLI.ATM.ATM2.Constants.sc;

@Controller
public class userController {

    @Autowired
    UserService userService;

    @Autowired
    BankService bankService;

    //signIn page
    @GetMapping("/")
    public String getSignInPage(Model model) {
        return "signInPage";
    }

    @PostMapping("/signInSubmit")
    public String getSignInDetails(@RequestParam("userid") int userid,
                                   @RequestParam("pin") String pin,
                                   @RequestParam("bank") int bank){
        System.out.println(userid);
        System.out.println(pin);
        System.out.println(bank);

        

        return "menuPage";
    }

    @PostMapping("/signUpSubmit")
    public String submitUser(UserInput userInput, UserService user, User userModel) {
        System.out.println(userInput.getFirstName());
        System.out.println(userInput.getLastName());
        System.out.println(userInput.getPin());
        System.out.println(userInput.getBankId());

        return "signInPage";
    }

    @GetMapping("/signup")
    public String getSignUpPage() {
        return "signUpPage";
    }

}
