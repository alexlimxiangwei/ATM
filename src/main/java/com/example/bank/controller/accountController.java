package com.example.bank.controller;

import com.example.bank.model.Account;
import com.example.bank.model.UserInput;
import com.example.bank.service.AccountService;
import com.example.bank.service.UserService;
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
