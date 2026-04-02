package com.e4net.pms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ManpowerSearchDto {
    private Long   projectId;    // 선택된 사업 ID (세션 기반 자동 필터)
    private String projectName;  // 사업명
    private String userName;     // 참여자명
    private String company;      // 소속회사
    private String status;       // 상태
}
