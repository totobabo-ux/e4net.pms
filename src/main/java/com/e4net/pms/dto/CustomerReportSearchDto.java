package com.e4net.pms.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class CustomerReportSearchDto {

    private Long   projectId;    // 세션 기반 자동 필터
    private String reportType;   // 보고구분 (정기보고용: 착수보고/중간보고/완료보고/기타보고, 주간보고용: 주간보고)
    private String reportName;   // 보고서명
    private String writer;       // 작성자
}
