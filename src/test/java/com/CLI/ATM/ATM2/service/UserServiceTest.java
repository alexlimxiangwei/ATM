package com.CLI.ATM.ATM2.service;

import com.CLI.ATM.ATM2.CLI.AccountCLI;
import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.Transaction;
import com.CLI.ATM.ATM2.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private SQLService sqlService;

    @Mock
    private AccountCLI accountCLI;


    @InjectMocks
    private UserService userService;

    private User user;

    private Account account;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User("John", "Doe", 123456, "1234", new ArrayList<Account>(), 1000, 500);
        account = new Account("John", 123456, user, new ArrayList<Transaction>(), 1000.00);
    }

    @Test
    public void testCreateNewUser() {
        when(sqlService.generateNewCustomerID()).thenReturn(111111);

        User newUser = userService.createNewUser("Jane", "Doe", "5678", 2000, 1000);

        Assertions.assertEquals("Jane", newUser.getFirstName());
        Assertions.assertEquals("Doe", newUser.getLastName());
        Assertions.assertEquals(111111, newUser.getCustomerID());
        Assertions.assertEquals("5678", newUser.getPinHash());

        verify(sqlService, times(1)).generateNewCustomerID();
    }

    @Test
    public void testAddAccountToUser() {
        userService.addAccountToUser(user, account);

        Assertions.assertEquals(1, user.getAccounts().size());
        Assertions.assertEquals(123456, user.getAccounts().get(0).getAccountID());
        Assertions.assertEquals(1000, user.getAccounts().get(0).getBalance());
    }

    @Test
    public void testNumAccounts() {
        Assertions.assertEquals(0, userService.numAccounts(user));

        Account account1 = new Account("John", 123456, user, new ArrayList<Transaction>(), 1000.00);
        userService.addAccountToUser(user, account1);

        Assertions.assertEquals(1, userService.numAccounts(user));

        Account account2 = account = new Account("Jenny", 123457, user, new ArrayList<Transaction>(), 1000.00);
        userService.addAccountToUser(user, account2);

        Assertions.assertEquals(2, userService.numAccounts(user));
    }

    @Test
    public void testValidatePin() {
        Assertions.assertFalse(userService.validatePin(user, "5678"));
    }


    @Test
    public void testGetAcctUUID() {
        userService.addAccountToUser(user, account);
        Assertions.assertEquals(123456, userService.getAcctUUID(user, 0));
    }

    @Test
    public void testGetAcct() {
        userService.addAccountToUser(user, account);

        Assertions.assertEquals(account, userService.getAcct(user, 0));
    }

    @Test
    public void testPrintAcctTransHistory() {

        userService.addAccountToUser(user, account);

        userService.printAcctTransHistory(user, 0);

        verify(accountCLI, times(1)).printTransHistory(account);
    }

    @Test
    public void testChangeAccountName() {
        User user = new User("John", "Doe", 1, "1234", new ArrayList<>(), 1000, 500);
        user.getAccounts().add(account);

        userService.changeAccountName(user, 0, "Checking");

        Assertions.assertEquals("Checking", user.getAccounts().get(0).getName());
    }

    @Test
    public void testDeleteAccount() {
        Account account1 = new Account("John", 123456, user, new ArrayList<Transaction>(), 1000.00);
        Account account2 = account = new Account("Jenny", 123457, user, new ArrayList<Transaction>(), 1000.00);
        User user = new User("John", "Doe", 1, "1234", new ArrayList<>(), 1000, 500);
        user.getAccounts().add(account1);
        user.getAccounts().add(account2);

        userService.deleteAccount(user, 1);

        Assertions.assertEquals(2, user.getAccounts().size());
        Assertions.assertEquals(account1, user.getAccounts().get(0));
    }


}