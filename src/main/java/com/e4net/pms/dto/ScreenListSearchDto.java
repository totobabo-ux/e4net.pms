package com.e4net.pms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ScreenListSearchDto {
    private Long   projectId;    // 세션 기반 자동 필터
    private String menuLevel1;   // 메뉴Level1
    private String category;     // 분류
    private String screenName;   // 화면명 (like 검색)
}
