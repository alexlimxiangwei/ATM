package com.CLI.ATM.ATM2.service;

import com.CLI.ATM.ATM2.model.Account;
import com.CLI.ATM.ATM2.model.Bank;
import com.CLI.ATM.ATM2.model.Transaction;
import com.CLI.ATM.ATM2.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BankServiceTest {

    private UserService userService;
    private AccountService accountService;
    private SQLService sqlService;
    private BankService bankService;

    private Bank mockBank;
    private Account mockAccount;

    @BeforeEach
    public void setUp() {
        userService = mock(UserService.class);
        accountService = mock(AccountService.class);
        sqlService = mock(SQLService.class);
        bankService = new BankService();
        bankService.userService = userService;
        bankService.accountService = accountService;
        bankService.SQLService = sqlService;
        mockBank = mock(Bank.class);
        mockAccount = mock(Account.class);
    }

    @Test
    public void testCreateNewBank() {
        int bankID = 123;
        String name = "Test Bank";
        boolean local = true;
        Bank bank = bankService.createNewBank(bankID, name, local);
        assertNotNull(bank);
        assertEquals(bankID, bank.getBankID());
        assertEquals(name, bank.getName());
        assertEquals(local, bank.isLocal());
    }

    @Test
    public void testAddUserToBank() {
        Bank bank = new Bank(0, "Test Bank", true, new ArrayList<User>(), new ArrayList<Account>());
        String firstName = "John";
        String lastName = "Doe";
        String pin = "1234";
        double localTransferLimit = 1000.0;
        double overseasTransferLimit = 500.0;
        User user = new User("John", "Doe", 1, "1234", new ArrayList<>(), 1000, 500);
        when(userService.createNewUser(firstName, lastName, pin, localTransferLimit, overseasTransferLimit)).thenReturn(user);
        Account account1 = new Account("SAVING", 123456, user, new ArrayList<Transaction>(), 0.00);
        when(accountService.createAccount("SAVING", user, 0.0)).thenReturn(account1);

        User newUser = bankService.addUserToBank(bank, firstName, lastName, pin, localTransferLimit, overseasTransferLimit);
        assertNotNull(newUser);
        assertEquals(1, bank.getUsers().size());
        assertEquals(user, bank.getUsers().get(0));
        assertEquals(1, bank.getAccounts().size());
        assertEquals(account1, bank.getAccounts().get(0));
        verify(userService, times(1)).createNewUser(firstName, lastName, pin, localTransferLimit, overseasTransferLimit);
        verify(accountService, times(1)).createAccount("SAVING", user, 0.0);
        verify(userService, times(1)).addAccountToUser(user, account1);
    }

    @Test
    void testUserLogin() {
        // Create a new User object
        User user = new User("John", "Doe", 123, "1234", new ArrayList<Account>(), 1000, 500);
        // Add the user to the bank's users list
        mockBank.getUsers().add(user);

        // Call the userLogin method with correct credentials
        User loggedInUser = userService.userLogin(mockBank, 123, "1234");

        // Call the userLogin method with incorrect credentials
        User nullUser = userService.userLogin(mockBank, 123, "4321");

        // Assert that null was returned
        assertNull(nullUser);
    }

    @Test
    void testGetBankFromID() {
        // Create a new Bank object
        Bank otherBank = mockBank;
        // Add the bank to an ArrayList of banks
        ArrayList<Bank> banks = new ArrayList<>();
        banks.add(otherBank);

        // Call the getBankFromID method with the bank's ID
        Bank retrievedBank = bankService.getBankFromID(banks, 0);

        // Assert that the correct Bank object was returned
        assertEquals(otherBank, retrievedBank);

        // Call the getBankFromID method with an invalid ID
        Bank nullBank = bankService.getBankFromID(banks, 1);

        // Assert that null was returned
        assertNull(nullBank);
    }

    @Test
    void testGetAccountFromID() {
        // Create a new Account object
        User user = new User("John", "Doe", 1, "1234", new ArrayList<>(), 1000, 500);
        Account account = new Account("SAVING", 123, user, new ArrayList<Transaction>(), 1000.00);
        // Add the account to the bank's accounts list
        mockBank.getAccounts().add(account);


        // Call the getAccountFromID method with an invalid ID
        Account nullAccount = bankService.getAccountFromID(mockBank, 456);

        // Assert that null was returned
        assertNull(nullAccount);
    }



}