package com.e4net.pms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RequirementSearchDto {
    private Long   projectId;   // 세션 기반 자동 필터
    private String title;       // 제목
    private String category;    // 분류
    private String priority;    // 우선순위
    private String status;      // 상태
}
