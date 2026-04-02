package com.e4net.pms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DeliverableSearchDto {
    private Long   projectId;       // 세션 기반 자동 필터
    private String deliverableType; // 산출물 구분
    private String stage;           // 단계
    private String name;            // 산출물명
}
