package com.e4net.pms.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class ProjectSearchDto {

    private String keyword;             // 프로젝트명 / 코드
    private String category;            // 구분
    private String company;             // 회사
    private String isPublicStr;         // 공개여부 ("true"/"false"/"")
    private String orderer;             // 발주처
    private String contractor;          // 계약처
    private LocalDate contractStartFrom;
    private LocalDate contractStartTo;
    private String pm;
    private List<String> statusList;    // 진행상태 (복수 선택)
}
