package com.e4net.pms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor
public class RequirementDto {

    private Long id;

    // ── 사업 ────────────────────────────────────────────────────
    @NotNull(message = "사업을 선택해주세요.")
    private Long projectId;              // ※ 사업명
    private String projectName;          // 표시용

    // ── 요구사항 정보 ────────────────────────────────────────────
    private String reqCode;              // 요구사항 코드

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;                // ※ 제목

    private String category;             // 분류 (기능/비기능)
    private String priority = "중";      // 우선순위 (상/중/하)
    private String status = "등록";      // 상태 (등록/분석중/개발중/완료/보류)
    private String requestor;            // 요청자
    private String description;          // 내용
    private String note;                 // 비고
    private String sourceType;           // 요구사항 출처 선택 (제안요청서/회의록/기타)
    private String sourceContent;        // 요구사항 출처 내용
    private String acceptance = "협의중"; // 수용여부 (협의중/수용/제외)

    private List<AttachFileDto> attachments; // 첨부파일 목록
}
