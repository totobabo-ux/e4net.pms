package com.e4net.pms.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class CommunitySearchDto {

    private String communityType;   // 고정 (컨트롤러에서 주입)
    private Long   projectId;       // 세션 사업 ID (null 이면 전체 조회)
    private String title;
    private String writer;
}
