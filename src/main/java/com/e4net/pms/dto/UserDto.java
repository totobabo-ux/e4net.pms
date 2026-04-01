package com.e4net.pms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserDto {
    private Long id;
    private String employeeNo;
    private String name;
    private String company;
    private String department;
    private String position;
    private String phone;
    private String email;
}
