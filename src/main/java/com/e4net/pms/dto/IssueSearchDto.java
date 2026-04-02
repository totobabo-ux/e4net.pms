package com.e4net.pms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class IssueSearchDto {
    private Long   projectId;    // 세션 기반 자동 필터
    private String issueName;    // 이슈명
    private String raiser;       // 제기자
    private String actionStatus; // 조치상태
}
