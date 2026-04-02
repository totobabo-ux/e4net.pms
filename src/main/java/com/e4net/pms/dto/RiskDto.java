package com.e4net.pms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class RiskDto {

    private Long id;

    // ── 사업 ────────────────────────────────────────────────────
    @NotNull(message = "사업을 선택해주세요.")
    private Long projectId;
    private String projectName;           // 표시용

    // ── 위험 기본 정보 ────────────────────────────────────────────
    private String riskCode;              // 위험코드

    @NotBlank(message = "위험명을 입력해주세요.")
    private String riskName;              // 위험명

    private String riskType;              // 위험유형: 기술/일정/비용/인력/외부
    private String identifiedDate;        // 식별일자 (String, LocalDate 변환 처리)
    private String description;           // 위험설명

    // ── 위험 평가 ──────────────────────────────────────────────────
    private String probability = "보통";  // 발생가능성: 낮음/보통/높음
    private String impact = "보통";       // 영향도: 적음/보통/심각/매우심각
    private String riskLevel;             // 위험등급: VERY LOW/LOW/MODERATE/HIGH/VERY HIGH

    // ── 대응 정보 ──────────────────────────────────────────────────
    private String responseStrategy;      // 대응전략: 회피/전가/완화/수용
    private String responsePlan;          // 대응계획
    private String owner;                 // 담당자

    // ── 활동결과 및 상태 ───────────────────────────────────────────
    private String activityResult;        // 활동결과
    private String status = "진행중";     // 위험상태: 진행중/해결/종료

    // ── 첨부파일 ──────────────────────────────────────────────────
    private List<AttachFileDto> attachments = new ArrayList<>();
}
