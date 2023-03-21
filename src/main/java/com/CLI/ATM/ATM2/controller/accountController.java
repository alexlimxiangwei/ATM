package com.CLI.ATM.ATM2.controller;

import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.UserInput;
import com.CLI.ATM.ATM2.service.AccountService;
import com.CLI.ATM.ATM2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class accountController {

    @Autowired
    UserService userService;


}
