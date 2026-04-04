package com.e4net.pms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InterfaceListSearchDto {
    private Long   projectId;       // 세션 기반 자동 필터
    private String linkType;        // 연계 구분
    private String interfaceMethod; // 인터페이스방식
    private String interfaceName;   // 인터페이스명 (like 검색)
}
