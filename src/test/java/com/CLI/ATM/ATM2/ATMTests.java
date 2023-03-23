package com.CLI.ATM.ATM2;

import com.CLI.ATM.ATM2.service.AccountService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ATMTests {

	@Test
	void contextLoads() {
	}

//	@Test
//	public void testValidateAmount() {
//		// test amount within limit
//		double amount = 500;
//		double limit = 1000;
//		String result = AccountService.validateAmount(amount, limit);
//		Assertions.assertNull(result);
//
//		// test amount equal to limit
//		amount = 1000;
//		result = AccountService.validateAmount(amount, limit);
//		Assertions.assertNull(result);
//
//		// test amount greater than limit
//		amount = 1500;
//		result = AccountService.validateAmount(amount, limit);
//		Assertions.assertEquals("Amount must not be greater than balance of $1000.00.", result);
//
//		// test negative amount
//		amount = -500;
//		result = AccountService.validateAmount(amount, limit);
//		Assertions.assertEquals("Amount must be greater than zero.", result);
//
//		// test zero amount
//		amount = 0;
//		result = AccountService.validateAmount(amount, limit);
//		Assertions.assertEquals("Amount must be greater than zero.", result);
//	}

}
