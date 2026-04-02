package com.e4net.pms.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class CustomerReportDto {

    private Long id;
    private Long projectId;
    private String projectName;          // 표시용

    // ── 보고 기본 정보 ─────────────────────────────────────────────
    private String reportType;           // 보고구분
    private String reportName;           // 보고서명
    private String reportDate;           // 보고일자 (yyyy-MM-dd)
    private String reportContent;        // 보고내용

    private String writer;               // 작성자

    // ── 첨부파일 ───────────────────────────────────────────────────
    private String attachFileName;       // 원본 파일명 (조회용)
    private String attachFilePath;       // 저장 경로 (읽기 전용)
}
