package com.e4net.pms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RiskSearchDto {
    private Long   projectId;   // 세션 기반 자동 필터
    private String riskName;    // 위험명
    private String riskType;    // 위험유형
    private String riskLevel;   // 위험등급
    private String status;      // 위험상태
}
