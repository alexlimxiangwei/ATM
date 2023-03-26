package com.CLI.ATM.ATM2.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInput {
    private String firstName;

    private String lastName;

    private String pin;

    private String bankId;

}
