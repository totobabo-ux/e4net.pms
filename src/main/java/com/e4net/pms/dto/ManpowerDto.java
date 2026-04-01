package com.e4net.pms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor
public class ManpowerDto {

    private Long id;

    // ── 사업 ────────────────────────────────────────────────────
    @NotNull(message = "사업을 선택해주세요.")
    private Long projectId;             // ※ 사업명
    private String projectName;         // 표시용

    // ── 참여자 ──────────────────────────────────────────────────
    @NotNull(message = "참여자를 선택해주세요.")
    private Long userId;                // ※ 참여자
    private String userName;            // 표시용

    // ── 소속 ────────────────────────────────────────────────────
    private String company;             // 소속회사
    private String department;          // 소속부서
    private String phone;               // 연락처 (자동채움)

    // ── 역할/등급 ────────────────────────────────────────────────
    @NotBlank(message = "역할을 입력해주세요.")
    private String role;                // ※ 역할

    private String position;            // 직위

    @NotBlank(message = "급수를 선택해주세요.")
    private String gradeCode;           // ※ 급수

    // ── 투입 정보 ────────────────────────────────────────────────
    @NotBlank(message = "투입구분을 선택해주세요.")
    private String inputTypeCode;       // ※ 투입구분

    @NotNull(message = "투입시작일을 선택해주세요.")
    private LocalDate inputStartDate;   // ※ 투입시작일

    @NotNull(message = "투입종료일을 선택해주세요.")
    private LocalDate inputEndDate;     // ※ 투입종료일

    private Double inputMm;             // 투입MM

    // ── 상태/비고 ────────────────────────────────────────────────
    private String status = "투입중";
    private String note;
}
