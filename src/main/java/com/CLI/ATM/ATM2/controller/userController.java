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

<<<<<<< Updated upstream
=======
    @RequestMapping(value = "/signInPage", method = RequestMethod.GET)
    public String populateList(Model model) {
        List<Bank> bankListing = bankService.fetchBanks();
        for (int i = 0; i < bankListing.size(); i++){
            if (!bankListing.get(i).isLocal()){
                bankListing.remove(i);
            }
        }
        model.addAttribute("bankListing", bankListing);
        return "signInPage.html";
    }

    @PostMapping("/signInPage")
    public String getSignInDetails(@RequestParam("userid") int userid,
                                   @RequestParam("pin") String pin,
                                   @RequestParam("bankDropdown") int bankid,
                                   Model model){

        populateList(model);
        Bank bankObj = BankService.getBankFromID(bankList, bankid);

        assert bankObj != null;
        User authUser = bankService.userLogin(bankObj, userid, pin);

        // print html authentication for userLogin
        String message = "No message here";
        if (authUser == null){
            message = "Wrong Login Credentials";
            model.addAttribute("message", message);
        }else{
            return "menuPage";
        }

        return "signInPage";
    }

    @PostMapping("/signUpPage")
    public String submitUser(@RequestParam("firstName") String firstName,
                             @RequestParam("lastName") String lastName,
                             @RequestParam("pin") String pin,
                             @RequestParam("bankDropdown") int bankid,
                             Model model) {

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
        userService.addNewUser(newUser2.getCustomerID(),firstName,lastName,newPin,currentBank);
        accountService.SQL_addAccount(newAccount2.getAccountID(),newUser2.getCustomerID(),currentBank.getBankID(),"Savings",0.00);

        System.out.println(newAccount2.getAccountID());
        System.out.println(currentBank.getBankID());

        return "signInPage";
    }

    @RequestMapping(value = "/signUpPage", method = RequestMethod.GET)
    public String populateList2(Model model) {
        List<Bank> bankListing = bankService.fetchBanks();
        for (int i = 0; i < bankListing.size(); i++){
            if (!bankListing.get(i).isLocal()){
                bankListing.remove(i);
            }
        }
        model.addAttribute("bankListing", bankListing);
        return "signUpPage.html";
    }
>>>>>>> Stashed changes
}
