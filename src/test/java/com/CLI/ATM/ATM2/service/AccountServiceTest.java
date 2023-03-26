package com.CLI.ATM.ATM2.service;

import com.CLI.ATM.ATM2.CLI.UserCLI;
import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.Transaction;
import com.CLI.ATM.ATM2.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.Assertions;
import static org.mockito.Mockito.*;

public class AccountServiceTest {

    @Mock
    UserService userService;

    @Mock
    TransactionService transactionService;

    @Mock
    UserCLI userCli;

    private SQLService sqlService;

    @Mock
    BankService bankService;

    @InjectMocks
    AccountService accountService;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User("John", "Doe", 123456, "1234", new ArrayList<Account>(), 1000, 500);
        MockitoAnnotations.initMocks(this);
        sqlService = mock(SQLService.class);
    }

    @Test
    public void testImportAccountFromSQL() {
        Account account = accountService.importAccountFromSQL(1001, "Savings", user, 500.0);
        Assertions.assertEquals("Savings", account.getName());
        Assertions.assertEquals(1001, account.getAccountID());
        Assertions.assertEquals(user, account.getUser());
        Assertions.assertEquals(500.0, account.getBalance(), 0);
    }

    @Test
    public void testAddBalance() {
        Account account = new Account("Savings", 1001, null, new ArrayList<>(), 500.0);
        accountService.addBalance(account, 200.0);
        Assertions.assertEquals(700.0, account.getBalance(), 0);
    }

    @Test
    public void testGetSummaryLine() {
        Account account = new Account("Savings", 1001, user, new ArrayList<>(), 500.0);
        HashMap<String, String> summary = accountService.getSummaryLine(account);
        Assertions.assertEquals("Savings", summary.get("name"));
        Assertions.assertEquals("1001", summary.get("uuid"));
        Assertions.assertEquals("500.00", summary.get("balance"));
    }
}