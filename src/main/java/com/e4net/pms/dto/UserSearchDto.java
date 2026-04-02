package com.e4net.pms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserSearchDto {
    private String employeeNo; // 사번
    private String name;       // 이름
    private String company;    // 소속회사
}
