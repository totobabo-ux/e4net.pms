package com.e4net.pms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BusinessFlowSearchDto {
    private Long   projectId;        // 세션 기반 자동 필터
    private String systemCategory;   // 시스템구분
    private String bizCategory;      // 업무구분
    private String processName;      // 프로세스명 (like 검색)
}
