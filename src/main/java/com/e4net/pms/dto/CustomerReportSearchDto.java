package com.e4net.pms.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor
public class CustomerReportSearchDto {

    private Long         projectId;    // 세션 기반 자동 필터
    private String       reportType;   // 보고구분 단일 값 (정확히 일치)
    private List<String> allowedTypes; // 허용 보고구분 목록 (IN 조건, 목록 필터 강제 적용)
    private String       reportName;   // 보고서명
    private String       writer;       // 작성자
}
