package com.e4net.pms.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor
public class ProjectSearchDto {

    private String keyword;              // 프로젝트명
    private String category;             // 구분
    private String company;              // 회사
    private String orderer;              // 발주처
    private String contractor;           // 계약처
    private LocalDate contractStartFrom;
    private LocalDate contractStartTo;
    private String pm;
}
