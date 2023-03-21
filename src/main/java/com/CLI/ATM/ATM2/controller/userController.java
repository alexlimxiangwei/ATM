package com.CLI.ATM.ATM2.controller;
import com.CLI.ATM.ATM2.CLI.AccountCLI;
import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.Transaction;
import com.CLI.ATM.ATM2.model.User;
import com.CLI.ATM.ATM2.model.UserInput;
import com.CLI.ATM.ATM2.service.BankService;
import com.CLI.ATM.ATM2.service.UserService;
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

    //signIn page
    @GetMapping("/")
    public String getSignInPage(Model model) {
        model.addAttribute("userInput", new UserInput());
        return "signInPage";
    }

    @PostMapping("/handleSubmit")
    public String submitUser(UserInput userInput, Account account, AccountCLI accountCLI) {
        System.out.println(userInput.getFirstName());
        System.out.println(userInput.getLastName());
        System.out.println(userInput.getPin());
        System.out.println(userInput.getBankId());

        System.out.println(account.getName());
        System.out.println(account.getAccountID());
        System.out.println(account.getBalance());

        return "menuPage";
    }

    @GetMapping("/signup")
    public String getSignUpPage() {
        return "signUpPage";
    }

}
