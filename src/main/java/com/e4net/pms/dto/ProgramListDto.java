package com.e4net.pms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor
public class ProgramListDto {

    private Long id;

    // ── 사업 ──────────────────────────────────────────────────
    @NotNull(message = "사업을 선택해주세요.")
    private Long projectId;
    private String projectName;

    // ── 프로그램 정보 ─────────────────────────────────────────
    private String systemName;       // 시스템구분

    private String programId;        // 프로그램ID (예: PGM-CMN-LGN-001)

    @NotBlank(message = "프로그램명을 입력해주세요.")
    private String programName;      // 프로그램명

    private String className;        // 클래스명

    private String classPath;        // 클래스경로

    private String programType;      // 프로그램구분 (공통코드 PROGRAM_TYPE)

    private String difficulty;       // 개발난이도 (공통코드 PROGRAM_DIFFICULTY)

    private String note;             // 비고

    // ── 첨부파일 ─────────────────────────────────────────────
    private List<AttachFileDto> attachments;
}
