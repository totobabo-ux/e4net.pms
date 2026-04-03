package com.e4net.pms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class BusinessFlowDto {

    private Long id;

    // ── 사업 ──────────────────────────────────────────────────
    @NotNull(message = "사업을 선택해주세요.")
    private Long projectId;
    private String projectName;

    // ── 업무흐름 정보 ─────────────────────────────────────────
    private String systemCategory;   // 시스템구분

    private String bizCategory;      // 업무구분

    private String processId;        // 프로세스ID

    @NotBlank(message = "프로세스명을 입력해주세요.")
    private String processName;      // 프로세스명 ※ 필수

    // ── 첨부파일 ──────────────────────────────────────────────
    private List<AttachFileDto> attachments = new ArrayList<>();
}
