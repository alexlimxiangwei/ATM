package com.example.bank.controller;

import com.example.bank.model.User;
import com.example.bank.model.UserInput;
import com.example.bank.service.BankService;
import com.example.bank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class userController {

    @Autowired
    UserService userService;

    @Autowired
    BankService bankService;

    @GetMapping("/")
    public String getSignInPage(Model model) {
        model.addAttribute("userInput", new UserInput());
        return "signInPage";
    }

    @PostMapping("/handleSubmit")
    public String submitUser(UserInput userInput) {
        System.out.println(userInput.getFirstName());
        System.out.println(userInput.getLastName());
        System.out.println(userInput.getPin());
        System.out.println(userInput.getBankId());



        return "menuPage";
    }

    @GetMapping("/signup")
    public String getSignUpPage() {
        return "signUpPage";
    }
}
