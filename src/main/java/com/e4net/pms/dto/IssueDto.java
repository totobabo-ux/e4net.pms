package com.e4net.pms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class IssueDto {

    private Long id;

    // ── 사업 ────────────────────────────────────────────────────
    @NotNull(message = "사업을 선택해주세요.")
    private Long projectId;
    private String projectName;           // 표시용

    // ── 이슈 기본 정보 ────────────────────────────────────────────
    private String issueNo;               // 관리번호

    @NotBlank(message = "이슈명을 입력해주세요.")
    private String issueName;             // 이슈명

    private String raiser;                // 제기자
    private String raisedDate;            // 제기일자 (String, LocalDate 변환)
    private String issueContent;          // 이슈내용

    // ── 조치 계획 ──────────────────────────────────────────────────
    private String actionPlanDate;        // 조치계획일자
    private String actionPlanContent;     // 조치계획내용

    // ── 조치 결과 ──────────────────────────────────────────────────
    private String actionStatus = "미조치"; // 조치상태: 미조치/조치중/조치완료/보류
    private String actionDate;            // 조치일자
    private String actionContent;         // 조치내용
    private String note;                  // 비고

    // ── 첨부파일 ──────────────────────────────────────────────────
    private List<AttachFileDto> attachments = new ArrayList<>();
}
