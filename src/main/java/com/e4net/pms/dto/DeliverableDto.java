package com.e4net.pms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class DeliverableDto {

    private Long id;

    // ── 사업 ──────────────────────────────────────────────────
    @NotNull(message = "사업을 선택해주세요.")
    private Long projectId;               // ※ 사업 ID
    private String projectName;           // 표시용 사업명

    // ── 산출물 정보 ──────────────────────────────────────────
    @NotBlank(message = "산출물 구분을 선택해주세요.")
    private String deliverableType;       // ※ 산출물 구분 (관리산출물/개발산출물)

    @NotBlank(message = "분류1을 입력해주세요.")
    private String category1;             // ※ 분류1

    @NotBlank(message = "분류2를 입력해주세요.")
    private String category2;             // ※ 분류2

    @NotBlank(message = "코드를 입력해주세요.")
    private String code;                  // ※ 코드

    @NotBlank(message = "산출물ID를 입력해주세요.")
    private String deliverableId;         // ※ 산출물ID

    @NotBlank(message = "산출물명을 입력해주세요.")
    private String name;                  // ※ 산출물명

    private String writtenYn;             // 작성여부
    private String stage = "미도래";       // 단계
    private String writer;                // 작성자
    private String note;                  // 비고
}
